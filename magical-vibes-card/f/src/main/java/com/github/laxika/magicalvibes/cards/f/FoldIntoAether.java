package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "31")
public class FoldIntoAether extends Card {

    public FoldIntoAether() {
        // Counter target spell. If that spell is countered this way, its controller may put a creature
        // card from their hand onto the battlefield. The counter is listed first so the rider sees the
        // outcome of the counter (CR 608.2c).
        PlayerSubject spellController = Subjects.controllerOf(Subjects.targetSpell());
        target(null, 1, 1)
                .addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new ConditionalEffect(
                        HappenedThisWay.countered(),
                        new MayEffect(
                                SequenceEffect.of(
                                        ChooseCardEffect.upTo(
                                                        Subjects.cardsInHands(spellController,
                                                                new CardTypePredicate(CardType.CREATURE)), 1,
                                                        "a creature card from your hand to put onto the battlefield")
                                                .by(spellController),
                                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                                "Put a creature card from your hand onto the battlefield?",
                                null,
                                MayChoicePlayer.TARGET_SPELL_CONTROLLER)));
    }
}
