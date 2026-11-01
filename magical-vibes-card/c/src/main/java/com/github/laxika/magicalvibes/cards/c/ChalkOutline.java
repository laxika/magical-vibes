package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "157")
public class ChalkOutline extends Card {

    public ChalkOutline() {
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)),
                        CreateTokenEffect.ofClueToken(1)));
    }
}
