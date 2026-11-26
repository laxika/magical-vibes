package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "206")
@CardRegistration(set = "SOS", collectorNumber = "354")
public class NitaForumConciliator extends Card {

    public NitaForumConciliator() {
        // Whenever you cast a spell you don't own, put a +1/+1 counter on each creature you control.
        addTrigger(Triggers.youCastSpell().withFilter(new CardControllerDoesNotOwnPredicate()),
                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));

        // {2}, Sacrifice another creature: Exile target instant or sorcery card from an opponent's
        // graveyard. You may cast it this turn, and mana of any type can be spent to cast that spell.
        // If that spell would be put into a graveyard, exile it instead. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        ExileGraveyardCardsEffect.oneTargetFromOpponentGraveyard(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))),
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))
                                .withAnyManaType().exilingInsteadOfGraveyard()
                ),
                "{2}, Sacrifice another creature: Exile target instant or sorcery card from an "
                        + "opponent's graveyard. You may cast it this turn, and mana of any type can be "
                        + "spent to cast that spell. If that spell would be put into a graveyard, exile it "
                        + "instead. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
