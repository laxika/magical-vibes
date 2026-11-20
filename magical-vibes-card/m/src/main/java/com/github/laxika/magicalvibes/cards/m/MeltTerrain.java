package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "97")
public class MeltTerrain extends Card {

    public MeltTerrain() {
        // Destroy target land. Melt Terrain deals 2 damage to that land's controller.
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(new DealDamageToPlayersEffect(2, Subjects.controllerOf(Subjects.target())));
    }
}
