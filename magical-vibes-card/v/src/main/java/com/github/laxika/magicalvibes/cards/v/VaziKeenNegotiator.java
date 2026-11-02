package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TreasuresCreatedThisTurn;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "92")
@CardRegistration(set = "NCC", collectorNumber = "110")
public class VaziKeenNegotiator extends Card {

    private static CardEffect counterAndDraw() {
        return SequenceEffect.of(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                new DrawCardEffect());
    }

    public VaziKeenNegotiator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetOpponentCreatesTokenEffect(
                        CreateTokenEffect.ofTreasureToken(new TreasuresCreatedThisTurn()))),
                "{T}: Target opponent creates X Treasure tokens, where X is the number of Treasure tokens you created this turn."));

        addTrigger(Triggers.opponentCastsSpell(),
                SpellCastTriggerEffect.usingTreasureMana(List.of(
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect())));
        addTrigger(Triggers.opponentActivatesAbility(),
                new ConditionalEffect(new TreasureManaSpentToActivate(), counterAndDraw()));
    }
}
