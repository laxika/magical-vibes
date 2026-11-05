package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "11")
public class Overcooked extends Card {

    public Overcooked() {
        addStaticEffect(new PlayersCantGainLifeEffect());

        PermanentEnteredThisTurn celebration = new PermanentEnteredThisTurn(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2);
        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new ConditionalEffect(celebration,
                        new ConjureCardNamedOntoBattlefieldEffect("WOE", "129")),
                new ConditionalEffect(new NotCondition(celebration), CreateTokenEffect.ofFoodToken(1))));
    }
}
