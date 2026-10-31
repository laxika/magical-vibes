package com.github.laxika.magicalvibes.cards.l;

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

@CardRegistration(set = "STH", collectorNumber = "61")
@CardRegistration(set = "TPR", collectorNumber = "108")
public class LabRats extends Card {

    public LabRats() {
        addStaticEffect(new BuybackEffect("{4}"));
        addSpellEffect(new CreateTokenEffect("Rat", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.RAT), Set.of(), Set.of()));
        addSpellEffect(new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
