package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CreatureCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

public class CreepingInn extends Card {

    public CreepingInn() {
        addTrigger(Triggers.attacks(), new MayEffect(SequenceEffect.of(
                ExileGraveyardCardsEffect.fromOwnGraveyardWithSource(1, new CardTypePredicate(CardType.CREATURE)),
                new ConditionalEffect(HappenedThisWay.exiled(), SequenceEffect.of(
                        new LoseLifeEffect(new CreatureCardsExiledWithSource(), Subjects.eachOpponent()),
                        new GainLifeEffect(new CreatureCardsExiledWithSource())))),
                "Exile a creature card from your graveyard?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new PhaseOutEffect(Subjects.source())),
                "{4}: Creeping Inn phases out."
        ));
    }
}
