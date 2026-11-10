package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "156")
public class BattleMadRonin extends Card {

    public BattleMadRonin() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));
        addStaticEffect(new MustAttackEffect());
    }
}
