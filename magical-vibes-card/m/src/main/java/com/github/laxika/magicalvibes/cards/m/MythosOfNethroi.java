package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "97")
public class MythosOfNethroi extends Card {

    public MythosOfNethroi() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnyOf(List.of(
                        new TargetPermanentMatches(new PermanentIsCreaturePredicate()),
                        new AllConditions(List.of(
                                new ColorSpentToCast(ManaColor.GREEN),
                                new ColorSpentToCast(ManaColor.WHITE)))
                )),
                new DestroyTargetPermanentEffect()));
    }
}
