package com.github.laxika.magicalvibes.model;

import java.util.List;

public record DeckValidation(List<String> errors, String legalityUpdatedAt) {
    public DeckValidation { errors = List.copyOf(errors); }
    public boolean valid() { return errors.isEmpty(); }
    public void requireValid() {
        if (!valid()) throw new IllegalArgumentException(String.join("; ", errors));
    }
}
