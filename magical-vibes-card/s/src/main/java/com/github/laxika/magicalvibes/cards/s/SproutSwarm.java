package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "138")
public class SproutSwarm extends Card {

    public SproutSwarm() {
        addStaticEffect(new BuybackEffect("{3}"));
        addSpellEffect(new CreateTokenEffect(
                "Saproling", 1, 1, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
        addSpellEffect(new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
