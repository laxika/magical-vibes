package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "30")
public class NomadMythmaker extends Card {

    public NomadMythmaker() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.BATTLEFIELD).filter(new CardIsAuraPredicate()).targetGraveyard(true).attachmentTarget(new PermanentIsCreaturePredicate()).build()), "{W}, {T}: Put target Aura card from a graveyard onto the battlefield under your control attached to a creature you control."));
    }
}
