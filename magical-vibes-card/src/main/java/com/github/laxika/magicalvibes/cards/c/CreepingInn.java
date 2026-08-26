package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreatureCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class CreepingInn extends Card {

    public CreepingInn() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new ExileOwnGraveyardCardThenEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        SequenceEffect.of(
                                new LoseLifeEffect(new CreatureCardsExiledWithSource(), LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(new CreatureCardsExiledWithSource())
                        ),
                        true
                ),
                "Exile a creature card from your graveyard?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new PhaseOutEffect(PhaseOutSubject.SOURCE)),
                "{4}: Creeping Inn phases out."
        ));
    }
}
