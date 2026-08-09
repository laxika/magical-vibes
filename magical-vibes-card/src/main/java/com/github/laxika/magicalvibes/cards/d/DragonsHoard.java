package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "232")
public class DragonsHoard extends Card {

    public DragonsHoard() {
        // Whenever a Dragon you control enters, put a gold counter on this artifact.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new PutCountersOnSelfEffect(CounterType.GOLD)));

        // {T}, Remove a gold counter from this artifact: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.GOLD),
                        new DrawCardEffect(1)
                ),
                "{T}, Remove a gold counter from this artifact: Draw a card."
        ));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
