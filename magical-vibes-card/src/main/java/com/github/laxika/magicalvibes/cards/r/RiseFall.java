package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "156")
public class RiseFall extends Card {

    public RiseFall() {
        TargetFilter creatureCard = new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS);
        TargetFilter creature = TargetFilters.creature();
        TargetFilter player = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");

        ChooseOneEffect.ChooseOneOption rise = new ChooseOneEffect.ChooseOneOption(
                "Rise — Return target creature card from a graveyard and target creature on the battlefield to their owners' hands",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .targetGraveyard(true)
                                .build(),
                        ReturnToHandEffect.target()),
                List.of(creatureCard, creature));

        ChooseOneEffect.ChooseOneOption fall = new ChooseOneEffect.ChooseOneOption(
                "Fall — Target player reveals two cards at random from their hand, then discards each nonland card revealed this way",
                new RevealRandomCardsFromTargetHandDiscardMatchingEffect(
                        2, new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                player);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                rise.withManaCost("{U}{B}"),
                fall.withManaCost("{B}{R}")
        )));
    }
}
