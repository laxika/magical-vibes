package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "YNEO", collectorNumber = "15")
public class UndercityPlunder extends Card {

    public UndercityPlunder() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addSpellEffect(new DiscardEffect(1, Subjects.targetPlayer()))
                .addSpellEffect(new MayEffect(
                        new DiscardEffect(1, Subjects.targetPlayer()),
                        "Discard an additional card?",
                        SequenceEffect.of(
                                ChooseCardEffect.atRandom(Subjects.cardsInLibraries(
                                        Subjects.targetPlayerDeclaredElsewhere(), null), "a card"),
                                ConjureCardsEffect.duplicateOf(Subjects.cardsThisWay(StepKind.CHOOSE),
                                        ConjureDestination.intoHand()),
                                new PerpetuallyGrantAnyColorManaToCastEffect(
                                        Subjects.cardsThisWay(StepKind.CONJURE))),
                        MayChoicePlayer.TARGET_PLAYER));
    }
}
