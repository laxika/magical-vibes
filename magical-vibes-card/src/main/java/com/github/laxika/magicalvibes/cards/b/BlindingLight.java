package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "8")
public class BlindingLight extends Card {

    public BlindingLight() {
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.WHITE)))));
    }
}
