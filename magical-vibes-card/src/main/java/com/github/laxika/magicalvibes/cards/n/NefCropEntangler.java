package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "144")
public class NefCropEntangler extends Card {

    public NefCropEntangler() {
        // Trample is a static keyword loaded from Scryfall. Exert: "You may exert this creature as it
        // attacks. When you do, it gets +1/+2 until end of turn." Modeled as an optional attack trigger
        // (matching Gust Walker). Choosing to exert also keeps the creature tapped through its next untap.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 2),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Nef-Crop Entangler as it attacks? (It gets +1/+2 until end of turn.)"
        ));
    }
}
