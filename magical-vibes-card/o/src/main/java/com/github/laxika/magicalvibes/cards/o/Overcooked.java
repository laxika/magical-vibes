package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YWOE", collectorNumber = "11")
public class Overcooked extends Card {

    public Overcooked() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());

        PermanentEnteredThisTurn celebration = new PermanentEnteredThisTurn(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(celebration,
                        new ConjureCardNamedOntoBattlefieldEffect("WOE", "129")),
                ConditionalEffect.unless(new NotCondition(celebration), CreateTokenEffect.ofFoodToken(1))));
    }
}
