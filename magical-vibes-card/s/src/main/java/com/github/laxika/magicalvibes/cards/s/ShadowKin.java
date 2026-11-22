package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "16")
@CardRegistration(set = "MIC", collectorNumber = "54")
public class ShadowKin extends Card {

    private static final CardTypePredicate CREATURE = new CardTypePredicate(CardType.CREATURE);

    public ShadowKin() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.eachPlayer()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.MILL, CREATURE),
                                new MayEffect(SequenceEffect.of(
                                        ChooseCardEffect.announced(Subjects.cardsThisWay(StepKind.MILL, CREATURE),
                                                "a creature card milled this way to exile"),
                                        ExileGraveyardCardsEffect.cards(Subjects.cardsThisWay(StepKind.CHOOSE)),
                                        new ConditionalEffect(HappenedThisWay.exiled(),
                                                BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.EXILE))
                                                        .keepingThisAbility())),
                                        "Exile a creature card milled this way and have Shadow Kin become a copy of it?"))));
    }
}
