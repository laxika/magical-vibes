package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "DRK", collectorNumber = "35")
public class Riptide extends Card {

    public Riptide() {
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
    }
}
