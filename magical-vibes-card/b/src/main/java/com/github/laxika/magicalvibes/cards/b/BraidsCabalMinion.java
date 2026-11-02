package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "117")
@CardRegistration(set = "EMA", collectorNumber = "82")
@CardRegistration(set = "MH2", collectorNumber = "273")
public class BraidsCabalMinion extends Card {

    public BraidsCabalMinion() {
        // At the beginning of each player's upkeep, that player sacrifices an artifact, creature,
        // or land of their choice.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate())),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
