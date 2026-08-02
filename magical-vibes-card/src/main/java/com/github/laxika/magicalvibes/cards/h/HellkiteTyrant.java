package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "GTC", collectorNumber = "94")
public class HellkiteTyrant extends Card {

    public HellkiteTyrant() {
        // Whenever this creature deals combat damage to a player, gain control of all artifacts that player controls.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GainControlOfAllPermanentsTargetPlayerControlsEffect(new PermanentIsArtifactPredicate()));

        // At the beginning of your upkeep, if you control twenty or more artifacts, you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControlsPermanentCount(20, new PermanentIsArtifactPredicate()),
                        new WinGameEffect()));
    }
}
