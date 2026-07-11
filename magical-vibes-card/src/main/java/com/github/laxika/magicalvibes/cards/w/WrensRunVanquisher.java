package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "LRW", collectorNumber = "245")
public class WrensRunVanquisher extends Card {

    public WrensRunVanquisher() {
        // As an additional cost to cast this spell, reveal an Elf card from your hand or pay {3}.
        // Deathtouch is intrinsic (auto-loaded from Scryfall keywords).
        addEffect(EffectSlot.STATIC, new IncreaseOwnCastCostUnlessRevealSubtypeEffect(3, CardSubtype.ELF));
    }
}
