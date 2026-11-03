package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "742")
public class IcemanAndFirestar extends Card {

    public IcemanAndFirestar() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
