package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrainTargetPlayersLandManaEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "5ED", collectorNumber = "82")
@CardRegistration(set = "4ED", collectorNumber = "67")
public class DrainPower extends Card {

    public DrainPower() {
        // Target player activates a mana ability of each land they control, then loses all
        // unspent mana and you add the mana lost this way.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrainTargetPlayersLandManaEffect());
    }
}
