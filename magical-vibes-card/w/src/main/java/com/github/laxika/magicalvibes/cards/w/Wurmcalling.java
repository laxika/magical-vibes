package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "234")
public class Wurmcalling extends Card {

    public Wurmcalling() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{2}{G}"));
        addSpellEffect(new CreateTokenEffect(
                "Wurm",
                new XValue(),
                new XValue(),
                CardColor.GREEN,
                List.of(CardSubtype.WURM),
                Set.of(),
                Set.of()));
        addSpellEffect(new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
