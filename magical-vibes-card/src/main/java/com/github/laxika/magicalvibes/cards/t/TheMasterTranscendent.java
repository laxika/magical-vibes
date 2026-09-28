package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "6")
@CardRegistration(set = "PIP", collectorNumber = "419")
@CardRegistration(set = "PIP", collectorNumber = "534")
@CardRegistration(set = "PIP", collectorNumber = "947")
public class TheMasterTranscendent extends Card {

    public TheMasterTranscendent() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GiveTargetPlayerRadCountersEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .targetPutIntoGraveyardFromLibraryThisTurn(true)
                        .battlefieldEffectGrants(List.of(
                                new GrantColorEffect(CardColor.GREEN, GrantScope.TARGET, true),
                                new GrantSubtypeEffect(CardSubtype.MUTANT, GrantScope.TARGET, true),
                                new SetBasePowerToughnessEffect(3, 3, GrantScope.TARGET,
                                        EffectDuration.PERMANENT)))
                        .build()),
                "Put target creature card in a graveyard that was milled this turn onto the battlefield under your control. "
                        + "It's a green Mutant with base power and toughness 3/3."
        ));
    }
}
