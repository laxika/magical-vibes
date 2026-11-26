package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ALA", collectorNumber = "59")
public class SteelcladSerpent extends Card {

    public SteelcladSerpent() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new ControlsAnotherPermanent(new PermanentIsArtifactPredicate()),
                "you control another artifact"
        ));
    }
}
