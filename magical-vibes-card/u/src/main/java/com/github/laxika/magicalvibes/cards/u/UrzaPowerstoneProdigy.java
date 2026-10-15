package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "69")
public class UrzaPowerstoneProdigy extends Card {

    public UrzaPowerstoneProdigy() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}, {T}: Draw a card, then discard a card."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new OncePerTurnTriggerEffect(CreateTokenEffect.ofPowerstoneToken(new Fixed(1)))));
    }
}
