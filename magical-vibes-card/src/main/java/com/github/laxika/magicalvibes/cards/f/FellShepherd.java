package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "78")
public class FellShepherd extends Card {

    public FellShepherd() {
        // Whenever Fell Shepherd deals combat damage to a player, you may return to your hand
        // all creature cards put into your graveyard from the battlefield this turn.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .returnAll(true)
                        .thisTurnOnly(true)
                        .build(),
                "Return creature cards to your hand?"));

        // {B}, Sacrifice another creature: Target creature gets -2/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new BoostTargetCreatureEffect(-2, -2)
                ),
                "{B}, Sacrifice another creature: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
