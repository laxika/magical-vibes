package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "74")
@CardRegistration(set = "KLR", collectorNumber = "111")
@CardRegistration(set = "2X2", collectorNumber = "99")
@CardRegistration(set = "SOC", collectorNumber = "232")
@CardRegistration(set = "CMM", collectorNumber = "201")
@CardRegistration(set = "TDC", collectorNumber = "202")
@CardRegistration(set = "LCC", collectorNumber = "214")
public class YahenniUndyingPartisan extends Card {

    public YahenniUndyingPartisan() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())
                ),
                "Sacrifice another creature: Yahenni, Undying Partisan gains indestructible until end of turn."
        ));
    }
}
