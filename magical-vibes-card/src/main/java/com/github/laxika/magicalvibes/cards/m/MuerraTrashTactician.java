package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "227")
public class MuerraTrashTactician extends Card {

    public MuerraTrashTactician() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaOfColorsEffect(
                        List.of(ManaColor.RED, ManaColor.GREEN),
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.RACCOON), CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.wheneverYouExpend(4, List.of(new GainLifeEffect(3))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.wheneverYouExpend(8,
                        List.of(new ExileTopCardsMayPlayUntilNextTurnEffect(2))));
    }
}
