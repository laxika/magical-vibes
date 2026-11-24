package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "121")
@CardRegistration(set = "PIP", collectorNumber = "649")
public class Vault11VotersDilemma extends Card {

    public Vault11VotersDilemma() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)),
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
        addTrigger(Triggers.chapter(2), votersDilemma());
        addTrigger(Triggers.chapter(3), votersDilemma());
    }

    /**
     * "Each player secretly votes for up to one creature, then those votes are revealed. If no creature got votes, each
     * player draws a card. Otherwise, destroy each creature with the most votes or tied for most votes."
     */
    private static CardEffect votersDilemma() {
        return SequenceEffect.of(
                VoteEffect.forPermanents(Subjects.allCreatures()).castSecretly().forUpToOne(),
                new ConditionalEffect(new NotCondition(new HappenedThisWay(StepKind.VOTE, 1)),
                        new EachPlayerDrawsCardEffect(1)),
                new DestroyTargetPermanentEffect(Subjects.mostVoted()));
    }
}
