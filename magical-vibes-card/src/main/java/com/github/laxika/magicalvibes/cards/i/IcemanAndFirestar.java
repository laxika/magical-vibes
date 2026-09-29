package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "742")
public class IcemanAndFirestar extends Card {

    public IcemanAndFirestar() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardColorPredicate(CardColor.BLUE),
                                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                                null,
                                TargetFilters.creature()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(
                                new CardColorPredicate(CardColor.RED),
                                List.of(new DiscardAndDrawCardEffect())),
                        "Discard a card to draw a card?"));
    }
}
