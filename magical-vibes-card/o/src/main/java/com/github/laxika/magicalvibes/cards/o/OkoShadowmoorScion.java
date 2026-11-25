package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemWithChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/** Back face of Oko, Lorwyn Liege. */
public class OkoShadowmoorScion extends Card {

    public OkoShadowmoorScion() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay("{U}", new TransformSelfEffect()).withPrompt("Pay {U} to transform Oko?"));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, new CardIsPermanentPredicate()), 1,
                                "a permanent card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))),
                "−1: Mill three cards. You may put a permanent card from among them into your hand."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect(2, "Elk", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.ELK), Set.of(), Set.of())),
                "−3: Create two 3/3 green Elk creature tokens."));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemWithChosenSubtypeEffect(
                        3,
                        3,
                        Set.of(Keyword.VIGILANCE, Keyword.HEXPROOF),
                        "Creatures you control of the chosen type get +3/+3 and have vigilance and hexproof.")),
                "−6: Choose a creature type. You get an emblem with \"Creatures you control of the chosen type get +3/+3 and have vigilance and hexproof.\""));
    }
}
