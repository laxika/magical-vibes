package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "31")
public class TahCropElite extends Card {

    public TahCropElite() {
        // Exert: "You may exert this creature as it attacks. When you do, creatures you control get
        // +1/+1 until end of turn." Modeled as an optional attack trigger (matching Glory-Bound
        // Initiate). Choosing to exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Tah-Crop Elite as it attacks? (Creatures you control get +1/+1 until end of turn.)"
        ));
    }
}
