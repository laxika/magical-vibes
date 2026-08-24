package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "TSP", collectorNumber = "103")
public class DemonicCollusion extends Card {

    public DemonicCollusion() {
        addEffect(EffectSlot.STATIC, new BuybackEffect(2));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
