package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "173")
public class HoodedBrawler extends Card {

    public HoodedBrawler() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +2/+2 until end of turn."
        // Modeled as an optional attack trigger (matching Nef-Crop Entangler / Gust Walker). Choosing to
        // exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(2, 2),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Hooded Brawler as it attacks? (It gets +2/+2 until end of turn.)"
        ));
    }
}
