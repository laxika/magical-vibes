package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOrLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "108")
public class LilianaWakerOfTheDead extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of combat on your turn, put target creature card from a graveyard "
                    + "onto the battlefield under your control. It gains haste.";

    public LilianaWakerOfTheDead() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new EachPlayerDiscardsOrLosesLifeEffect(3)),
                "+1: Each player discards a card. Each opponent who can't loses 3 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new BoostTargetCreatureEffect(graveyardCardsNegated(), graveyardCardsNegated())),
                "-3: Target creature gets -X/-X until end of turn, where X is the number of cards in your graveyard.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.BEGINNING_OF_COMBAT,
                                List.of(ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                        .targetGraveyard(true)
                                        .grantHaste(true)
                                        .build()),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "-7: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }

    private static Scaled graveyardCardsNegated() {
        return new Scaled(new CardsInGraveyard(null, CountScope.CONTROLLER), -1);
    }
}
