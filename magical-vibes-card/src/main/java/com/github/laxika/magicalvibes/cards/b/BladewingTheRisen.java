package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "1")
public class BladewingTheRisen extends Card {

    public BladewingTheRisen() {
        // When Bladewing enters, you may return target Dragon permanent card from your
        // graveyard to the battlefield. (Modeled as up-to-one graveyard target.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardSubtypePredicate(CardSubtype.DRAGON))
                .targetGraveyard(true)
                .build());

        // {B}{R}: Dragon creatures get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}{R}",
                List.of(new BoostAllCreaturesEffect(1, 1, new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                "{B}{R}: Dragon creatures get +1/+1 until end of turn."));
    }
}
