package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "156")
public class MurkfiendLiege extends Card {

    public MurkfiendLiege() {
        // Other green creatures you control get +1/+1. (OWN_CREATURES scope excludes the source itself.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        // Other blue creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));

        // Untap all green and/or blue creatures you control during each other player's untap step.
        addEffect(EffectSlot.STATIC, new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.BLUE))))));
    }
}
