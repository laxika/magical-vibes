package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "102")
public class RankOfficer extends Card {

    public RankOfficer() {
        // When this creature enters, you may discard a card. If you do, create a 2/2 black Zombie
        // creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                CreateTokenEffect.blackZombie(1))),
                "Discard a card to create a 2/2 black Zombie creature token?"
        ));

        // {1}{B}, {T}, Exile a creature card from your graveyard: Each opponent loses 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new LoseLifeEffect(2, Subjects.eachOpponent())
                ),
                "{1}{B}, {T}, Exile a creature card from your graveyard: Each opponent loses 2 life."
        ));
    }
}
