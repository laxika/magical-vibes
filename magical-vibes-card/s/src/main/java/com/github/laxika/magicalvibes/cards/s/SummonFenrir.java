package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "203")
@CardRegistration(set = "FIN", collectorNumber = "372")
public class SummonFenrir extends Card {

    public SummonFenrir() {
        addTrigger(Triggers.chapter(1),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        addTrigger(Triggers.chapter(2), new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalCountersToCastSpellEffect(new Fixed(1))), true, false));

        addTrigger(Triggers.chapter(3), new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(), new DrawCardEffect(1)));
    }
}
