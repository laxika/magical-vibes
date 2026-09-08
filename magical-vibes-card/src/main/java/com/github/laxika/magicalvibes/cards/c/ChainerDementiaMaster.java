package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "56")
public class ChainerDementiaMaster extends Card {

    public ChainerDementiaMaster() {
        // All Nightmares get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.NIGHTMARE)));

        // {B}{B}{B}, Pay 3 life: Put target creature card from a graveyard onto the battlefield
        // under your control. That creature is a black Nightmare in addition to its other colors
        // and types.
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}{B}", List.of(
                new PayLifeCost(3),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .grantColor(CardColor.BLACK)
                        .grantSubtype(CardSubtype.NIGHTMARE)
                        .build()),
                "{B}{B}{B}, Pay 3 life: Put target creature card from a graveyard onto the battlefield under your control. "
                        + "That creature is black and is a Nightmare in addition to its other creature types."));

        // When Chainer leaves the battlefield, exile all Nightmares.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ExileAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.NIGHTMARE)));
    }
}
