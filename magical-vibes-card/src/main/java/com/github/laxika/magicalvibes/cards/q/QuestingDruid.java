package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeekTheBeast;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "234")
public class QuestingDruid extends Card {

    public QuestingDruid() {
        setBackFaceCard(new SeekTheBeast());
        addCastingOption(new AdventureCast("{1}{R}"));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.BLACK),
                        new CardColorPredicate(CardColor.RED))),
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeekTheBeast";
    }
}
