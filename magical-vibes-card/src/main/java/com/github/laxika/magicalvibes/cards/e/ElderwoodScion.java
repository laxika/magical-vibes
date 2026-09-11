package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "PC2", collectorNumber = "88")
public class ElderwoodScion extends Card {

    public ElderwoodScion() {
        PermanentIsSourcePermanentPredicate sourcePredicate = new PermanentIsSourcePermanentPredicate();

        // Spells you cast that target this creature cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(sourcePredicate, 2, true));

        // Spells your opponents cast that target this creature cost {2} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                sourcePredicate, 2, false));
    }
}
