package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromOpponentGraveyardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MKM", collectorNumber = "83")
@CardRegistration(set = "MKM", collectorNumber = "399")
public class DeadlyCoverUp extends Card {

    public DeadlyCoverUp() {
        addEffect(EffectSlot.SPELL, new CollectEvidenceCost(6, true));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CollectEvidenceCostPaid(), new ExileCardFromOpponentGraveyardAndSameNameFromZonesEffect()));
    }
}
