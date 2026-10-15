package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "217")
public class SpectralForce extends Card {

    public SpectralForce() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new NotCondition(new DefendingPlayerControlsPermanent(
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)))),
                        new SkipNextUntapEffect(TapUntapScope.SELF)));
    }
}
