package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "93")
@CardRegistration(set = "PIP", collectorNumber = "337")
@CardRegistration(set = "PIP", collectorNumber = "621")
@CardRegistration(set = "PIP", collectorNumber = "865")
public class ArmoryPaladin extends Card {

    public ArmoryPaladin() {
        CardPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
        addTrigger(Triggers.youCastSpell().withFilter(auraOrEquipment), new ExileTopCardMayPlayThisTurnEffect(1, false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
