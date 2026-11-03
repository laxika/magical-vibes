package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "58")
@CardRegistration(set = "MUL", collectorNumber = "123")
@CardRegistration(set = "MUL", collectorNumber = "188")
@CardRegistration(set = "DMU", collectorNumber = "216")
public class RonaSheoldredsFaithful extends Card {

    public RonaSheoldredsFaithful() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));

        addCastingOption(new GraveyardCast(List.of(new DiscardCardCastingCost(null, null, 2))));
    }
}
