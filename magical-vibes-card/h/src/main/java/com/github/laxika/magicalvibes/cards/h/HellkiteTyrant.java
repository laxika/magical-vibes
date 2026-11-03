package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "94")
@CardRegistration(set = "RVR", collectorNumber = "111")
@CardRegistration(set = "FIC", collectorNumber = "295")
@CardRegistration(set = "C21", collectorNumber = "172")
@CardRegistration(set = "C16", collectorNumber = "128")
public class HellkiteTyrant extends Card {

    public HellkiteTyrant() {
        // Whenever this creature deals combat damage to a player, gain control of all artifacts that player controls.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GainControlOfAllPermanentsTargetPlayerControlsEffect(new PermanentIsArtifactPredicate()));

        // At the beginning of your upkeep, if you control twenty or more artifacts, you win the game.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanentCount(20, new PermanentIsArtifactPredicate())),
                new WinGameEffect());
    }
}
