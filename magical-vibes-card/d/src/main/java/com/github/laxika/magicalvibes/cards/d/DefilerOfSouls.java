package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "37")
public class DefilerOfSouls extends Card {

    public DefilerOfSouls() {
        // Flying is auto-loaded from Scryfall.
        // At the beginning of each player's upkeep, that player sacrifices a monocolored creature
        // of their choice. Triggers.upkeep(ActivePlayer.ANY) sets the active player as the target that sacrifices.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsMonocoloredPredicate())),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
