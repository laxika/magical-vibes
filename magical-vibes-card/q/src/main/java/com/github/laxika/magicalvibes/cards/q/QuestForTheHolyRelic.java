package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForEquipmentToBattlefieldAndAttachEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "33")
public class QuestForTheHolyRelic extends Card {

    public QuestForTheHolyRelic() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for the Holy Relic?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new SearchLibraryForEquipmentToBattlefieldAndAttachEffect()
                ),
                "Remove five quest counters from Quest for the Holy Relic and sacrifice it: Search your library for an Equipment card, put it onto the battlefield, attach it to a creature you control, then shuffle."
        ));
    }
}
