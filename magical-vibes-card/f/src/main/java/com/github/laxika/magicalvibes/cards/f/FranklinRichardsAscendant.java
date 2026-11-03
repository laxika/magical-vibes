package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "52")
@CardRegistration(set = "MSC", collectorNumber = "362")
public class FranklinRichardsAscendant extends Card {

    public FranklinRichardsAscendant() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                new DiscoverEffect(6));
    }
}
