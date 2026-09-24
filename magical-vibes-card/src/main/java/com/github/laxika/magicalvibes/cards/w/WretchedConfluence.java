package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "200")
@CardRegistration(set = "CMM", collectorNumber = "527")
@CardRegistration(set = "C15", collectorNumber = "23")
public class WretchedConfluence extends Card {

    public WretchedConfluence() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target player draws a card and loses 1 life",
                        () -> SequenceEffect.of(
                                new DrawCardForTargetPlayerEffect(1, false, true),
                                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                        anyPlayer()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target creature gets -2/-2 until end of turn",
                        () -> new BoostTargetCreatureEffect(-2, -2),
                        TargetFilters.creature()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Return target creature card from your graveyard to your hand",
                        () -> ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(creatureCard)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(creatureCard, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        ), 3));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
    }
}
