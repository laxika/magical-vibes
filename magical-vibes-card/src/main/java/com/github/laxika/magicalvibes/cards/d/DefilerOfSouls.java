package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "37")
public class DefilerOfSouls extends Card {

    public DefilerOfSouls() {
        // Flying is auto-loaded from Scryfall.
        // At the beginning of each player's upkeep, that player sacrifices a monocolored creature
        // of their choice. EACH_UPKEEP_TRIGGERED sets the active player as the target that sacrifices.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsMonocoloredPredicate())),
                SacrificeRecipient.TARGET_PLAYER));
    }
}
