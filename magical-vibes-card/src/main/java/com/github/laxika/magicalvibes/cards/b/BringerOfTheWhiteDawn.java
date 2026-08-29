package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "7")
public class BringerOfTheWhiteDawn extends Card {

    public BringerOfTheWhiteDawn() {
        // You may pay {W}{U}{B}{R}{G} rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}{U}{B}{R}{G}"))));

        // At the beginning of your upkeep, you may return target artifact card from your graveyard
        // to the battlefield. The optional choice is modeled as an up-to-one graveyard target.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
