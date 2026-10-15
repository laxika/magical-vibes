package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "266")
@CardRegistration(set = "C19", collectorNumber = "57")
@CardRegistration(set = "ZNC", collectorNumber = "117")
public class Scaretiller extends Card {

    public Scaretiller() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        ReturnCardFromGraveyardEffect returnLand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .filter(land)
                .targetGraveyard(true)
                .enterTapped(true)
                .build();

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "You may put a land card from your hand onto the battlefield tapped",
                                new MayEffect(
                                        new PutCardToBattlefieldEffect(land, "land", true),
                                        "Put a land card from your hand onto the battlefield tapped?")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Return target land card from your graveyard to the battlefield tapped",
                                returnLand,
                                new GraveyardCardPredicateTargetFilter(
                                        land, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                )))));
    }
}
