package edu.iis.mto.testreactor.washingmachine;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class ProgramConfiguration {

    private final Program program;
    private final boolean spin;

    ProgramConfiguration(Builder builder) {
        this.program = requireNonNull(builder.program, "program == null");
        this.spin = builder.spin;
    }

    public Program getProgram() {
        return program;
    }

    public boolean isSpin() {
        return spin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(program, spin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProgramConfiguration other = (ProgramConfiguration) obj;
        return program == other.program && spin == other.spin;
    }

    @Override
    public String toString() {
        return "ProgramConfiguration [program=" + program + ", spin=" + spin + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Program program;
        private boolean spin = true;

        private Builder() {}

        public Builder withProgram(Program program) {
            this.program = program;
            return this;
        }

        public Builder withSpin(boolean spin) {
            this.spin = spin;
            return this;
        }

        public ProgramConfiguration build() {
            return new ProgramConfiguration(this);
        }
    }
}
