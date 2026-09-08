package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardFromTargetGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "92")
public class DreamsOfSteelAndOil extends Card {

    public DreamsOfSteelAndOil() {
        var artifactOrCreature = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent."))
                .addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                        1, List.of(), artifactOrCreature, HandChoiceDestination.EXILE));
        addEffect(EffectSlot.SPELL, new ExileMatchingCardFromTargetGraveyardEffect(artifactOrCreature));
    }
}
