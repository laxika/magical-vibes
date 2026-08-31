package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "74")
public class ChokingTethers extends Card {

    public ChokingTethers() {
        target(TargetFilters.creature(), 0, 4)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new DrawCardEffect(1)),
                "Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.)",
                TargetFilters.creature(), null, null, null, List.of(), 0, 1));
    }
}
