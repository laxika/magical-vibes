package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "97")
@CardRegistration(set = "PIP", collectorNumber = "410")
@CardRegistration(set = "PIP", collectorNumber = "625")
@CardRegistration(set = "PIP", collectorNumber = "938")
public class CassHandOfVengeance extends Card {

    public CassHandOfVengeance() {
        addEffect(EffectSlot.ON_DEATH, deathEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, deathEffect());
    }

    private CardEffect deathEffect() {
        return new TriggeringPermanentConditionalEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsEnchantedPredicate(),
                        new PermanentIsEquippedPredicate())),
                new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect());
    }
}
