package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "153")
public class TrueheartTwins extends Card {

    public TrueheartTwins() {
        // Exert: "You may exert this creature as it attacks." (Plain Exert, no self-bonus.) Modeled as an
        // optional attack trigger (matching Battlefield Scavenger); choosing to exert keeps the creature
        // tapped through its next untap step.
        //
        // "Whenever you exert a creature, creatures you control get +1/+0 until end of turn." The engine has
        // no exert-event slot, so the only exert it can observe is this creature's own exert as it attacks —
        // the unconditional +1/+0 boost is bundled onto the exert when it is accepted.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new BoostAllOwnCreaturesEffect(1, 0)
                ),
                "Exert Trueheart Twins as it attacks? (Creatures you control get +1/+0 until end of turn.)"
        ));
    }
}
