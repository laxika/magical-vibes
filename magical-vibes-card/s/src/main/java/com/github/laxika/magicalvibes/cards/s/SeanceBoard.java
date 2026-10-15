package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "40")
@CardRegistration(set = "DSC", collectorNumber = "67")
public class SeanceBoard extends Card {

    public SeanceBoard() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new Morbid(), new PutCountersOnSelfEffect(CounterType.SOUL)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        new CountersOnSource(CounterType.SOUL),
                        ManaSpendRestriction.INSTANT_SORCERY_OR_SUBTYPES,
                        null,
                        false)),
                "{T}: Add X mana of any one color, where X is the number of soul counters on this artifact. Spend this mana only to cast instant, sorcery, Demon, and Spirit spells."
        ));
    }
}
