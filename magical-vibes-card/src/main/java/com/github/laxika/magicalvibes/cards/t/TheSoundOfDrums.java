package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DoubleEnchantedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "97")
@CardRegistration(set = "WHO", collectorNumber = "391")
@CardRegistration(set = "WHO", collectorNumber = "702")
@CardRegistration(set = "WHO", collectorNumber = "982")
public class TheSoundOfDrums extends Card {

    public TheSoundOfDrums() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GoadEquippedCreatureEffect())
                .addEffect(EffectSlot.STATIC, new DoubleEnchantedCreatureCombatDamageEffect());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{R}: Return this card from your graveyard to your hand."
        ));
    }
}
