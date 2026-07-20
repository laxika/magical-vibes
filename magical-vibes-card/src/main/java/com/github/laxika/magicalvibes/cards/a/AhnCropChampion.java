package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "AKH", collectorNumber = "194")
public class AhnCropChampion extends Card {

    public AhnCropChampion() {
        // Exert: "You may exert this creature as it attacks. When you do, untap all other creatures
        // you control." Modeled as an optional attack trigger (matching Combat Celebrant). Choosing to
        // exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Ahn-Crop Champion as it attacks? (Untap all other creatures you control.)"
        ));
    }
}
