package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.SeekToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "30")
public class SpiritedSimulacrum extends Card {

    public SpiritedSimulacrum() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SeekToBattlefieldEffect(new CardTypePredicate(CardType.LAND)));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new SeekEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}"))));
    }
}
