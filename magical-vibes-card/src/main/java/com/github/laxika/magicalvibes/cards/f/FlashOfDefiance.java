package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "99")
public class FlashOfDefiance extends Card {

    public FlashOfDefiance() {
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{1}{R}"),
                new LifeCastingCost(3))));
    }
}
