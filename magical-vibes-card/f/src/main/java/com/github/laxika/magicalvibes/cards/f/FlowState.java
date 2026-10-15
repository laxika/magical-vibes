package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "49")
public class FlowState extends Card {

    public FlowState() {
        var instantAndSorceryInGraveyard = new AllConditions(List.of(
                new GraveyardCardThreshold(1, new CardTypePredicate(CardType.INSTANT)),
                new GraveyardCardThreshold(1, new CardTypePredicate(CardType.SORCERY))));
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(3),
                new FixedIfCondition(instantAndSorceryInGraveyard, 2, 1),
                null,
                LookDestination.BOTTOM_OF_LIBRARY,
                false));
    }
}
