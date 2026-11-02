package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "17")
public class ConsumingOni extends Card {

    public ConsumingOni() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect(
                        Triggers.youCastThisSpell(),
                        List.of(new LoseLifeEffect(3)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
