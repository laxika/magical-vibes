package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Resolves Champion of the Hareish's buddy-list trigger for one entering creature. */
public record ChampionOfTheHareishTriggerEffect(List<CardSubtype> enteringSubtypes) implements CardEffect {

    public ChampionOfTheHareishTriggerEffect() {
        this(List.of());
    }

    public ChampionOfTheHareishTriggerEffect {
        enteringSubtypes = enteringSubtypes == null ? List.of() : List.copyOf(enteringSubtypes);
    }
}
