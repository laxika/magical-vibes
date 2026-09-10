package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "11")
public class TheFourthSphere extends Card {

    public TheFourthSphere() {
        PermanentPredicate nonblackCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificePermanentsEffect(1, nonblackCreature, SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.CHAOS_TRIGGERED, CreateTokenEffect.blackZombie(1));
    }
}
