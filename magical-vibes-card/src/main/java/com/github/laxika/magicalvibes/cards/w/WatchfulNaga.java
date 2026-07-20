package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "193")
public class WatchfulNaga extends Card {

    public WatchfulNaga() {
        // Exert: "You may exert this creature as it attacks. When you do, draw a card."
        // Modeled as an optional attack trigger (matching Hooded Brawler / Nef-Crop Entangler). Choosing to
        // exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Watchful Naga as it attacks? (Draw a card.)"
        ));
    }
}
