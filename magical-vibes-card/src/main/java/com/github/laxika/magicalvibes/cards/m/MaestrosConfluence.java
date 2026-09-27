package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesTargetPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "75")
@CardRegistration(set = "NCC", collectorNumber = "175")
public class MaestrosConfluence extends Card {

    public MaestrosConfluence() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        CardPredicate monocoloredInstantOrSorcery = new CardAllOfPredicate(List.of(
                new CardHasExactlyNColorsPredicate(1), instantOrSorcery));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Return target monocolored instant or sorcery card from your graveyard to your hand",
                        () -> ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(monocoloredInstantOrSorcery)
                                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                monocoloredInstantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target creature gets -3/-3 until end of turn",
                        () -> new BoostTargetCreatureEffect(-3, -3),
                        TargetFilters.creature()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Goad each creature target player controls",
                        GoadCreaturesTargetPlayerControlsUntilNextTurnEffect::new,
                        anyPlayer())
        ), 3));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
    }
}
