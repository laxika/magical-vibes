package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "FUT", collectorNumber = "81")
public class BridgeFromBelow extends Card {

    public BridgeFromBelow() {
        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new ConditionalEffect(new SourceCardInGraveyard(), CreateTokenEffect.blackZombie(1))));
        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new ConditionalEffect(new SourceCardInGraveyard(), new ExileSourceCardFromGraveyardEffect()));
    }
}
