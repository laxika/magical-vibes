package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksAnotherOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1791")
public class KarazikarTheEyeTyrant extends Card {

    public KarazikarTheEyeTyrant() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate()
                )),
                "Target must be a creature the attacked player controls"))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                        new GoadTargetCreatureUntilNextTurnEffect());

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new OpponentAttacksAnotherOpponent(),
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new LoseLifeEffect(1),
                                new DrawCardForTargetPlayerEffect(1),
                                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER)
                        )));
    }
}
