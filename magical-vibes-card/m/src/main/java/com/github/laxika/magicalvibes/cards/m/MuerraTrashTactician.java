package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "227")
public class MuerraTrashTactician extends Card {

    public MuerraTrashTactician() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardManaOfColorsEffect(
                        List.of(ManaColor.RED, ManaColor.GREEN),
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.RACCOON), CountScope.CONTROLLER)));
        addTrigger(Triggers.youCastSpell().expend(4), new GainLifeEffect(3));
        addTrigger(Triggers.youCastSpell().expend(8), new ExileTopCardsMayPlayUntilNextTurnEffect(2));
    }
}
