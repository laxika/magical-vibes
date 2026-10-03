package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "16")
@CardRegistration(set = "BRC", collectorNumber = "63")
public class MachineGodsEffigy extends Card {

    public MachineGodsEffigy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CopyPermanentOnEnterEffect.withCardTypesOverride(
                new PermanentIsCreaturePredicate(), "creature", Set.of(CardType.ARTIFACT), Set.of(),
                List.of(ManaAbilities.tapFor(ManaColor.BLUE))));
    }
}
