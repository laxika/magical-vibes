package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackDamagedPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "10")
@CardRegistration(set = "MSC", collectorNumber = "293")
public class SilverSurferGalactussHerald extends Card {

    public SilverSurferGalactussHerald() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Galactus, Devourer of Worlds"),
                        LibrarySearchDestination.HAND),
                "Search your library for a card named Galactus, Devourer of Worlds?"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new TargetCreatureMustAttackDamagedPlayerUntilNextTurnEffect());
    }
}
