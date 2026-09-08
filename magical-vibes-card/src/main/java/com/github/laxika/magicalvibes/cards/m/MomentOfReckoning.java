package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "205")
public class MomentOfReckoning extends Card {

    private static final CardPredicate NONLAND_PERMANENT_CARD = new CardAllOfPredicate(List.of(
            new CardIsPermanentPredicate(),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

    public MomentOfReckoning() {
        var nonlandPermanent = TargetFilters.nonlandPermanent();
        addEffect(EffectSlot.SPELL, ChooseOneEffect.upToWithRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Destroy target nonland permanent",
                        () -> new DestroyTargetPermanentEffect(nonlandPermanent.predicate()),
                        nonlandPermanent),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Return target nonland permanent card from your graveyard to the battlefield",
                        () -> ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(NONLAND_PERMANENT_CARD)
                                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                NONLAND_PERMANENT_CARD, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        ), 4));
    }
}
