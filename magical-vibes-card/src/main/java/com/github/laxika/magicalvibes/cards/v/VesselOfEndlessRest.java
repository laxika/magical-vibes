package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "224")
public class VesselOfEndlessRest extends Card {

    public VesselOfEndlessRest() {
        // When this artifact enters, put target card from a graveyard on the bottom of its owner's library.
        // The ETB pipeline has no graveyard-target selector, so this resolves as a mandatory
        // resolution-time choice across all graveyards (identical in practice).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .build());

        addActivatedAbility(new ActivatedAbility(
                true,                                   // requiresTap
                "",                                     // manaCost
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
    }
}
