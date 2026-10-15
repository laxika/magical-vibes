package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "ODY", collectorNumber = "276")
public class StoneTongueBasilisk extends Card {

    public StoneTongueBasilisk() {
        // Whenever this creature deals combat damage to a creature, destroy that creature at end of combat.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentIsSourcePermanentPredicate()));

        // Threshold — As long as there are seven or more cards in your graveyard, all creatures able
        // to block this creature do so.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new GraveyardCardThreshold(7, null),
                new MustBeBlockedByAllCreaturesEffect()));
    }
}
