package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsDestroyedThisWayFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "TOR", collectorNumber = "3")
public class CleansingMeditation extends Card {

    public CleansingMeditation() {
        DestroyAllPermanentsEffect destroyAllEnchantments = new DestroyAllPermanentsEffect(
                new PermanentIsEnchantmentPredicate());
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(7, null),
                destroyAllEnchantments,
                SequenceEffect.of(
                        destroyAllEnchantments,
                        new ReturnCardsDestroyedThisWayFromGraveyardEffect())));
    }
}
