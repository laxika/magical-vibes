package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "225")
public class SavraQueenOfTheGolgari extends Card {

    public SavraQueenOfTheGolgari() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        creatureOfColor(CardColor.BLACK),
                        new MayPayLifeEffect(2,
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        SacrificeRecipient.EACH_OPPONENT),
                                "Pay 2 life?")));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        creatureOfColor(CardColor.GREEN),
                        new MayEffect(new GainLifeEffect(2), "Gain 2 life?")));
    }

    private static PermanentAllOfPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
