package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1874")
public class AnjeFalkenrath extends Card {

    public AnjeFalkenrath() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{T}, Discard a card: Draw a card."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new TriggeringCardConditionalEffect(
                        new CardKeywordPredicate(Keyword.MADNESS),
                        new UntapPermanentsEffect(TapUntapScope.SELF)));
    }
}
