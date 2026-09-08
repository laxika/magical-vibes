package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "2")
public class BenalishCommander extends Card {

    public BenalishCommander() {
        PermanentCount soldiersYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(soldiersYouControl, soldiersYouControl));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                CreateTokenEffect.whiteSoldier(1));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{W}{W}",
                List.of(),
                "Suspend X—{X}{W}{W}",
                ActivationTimingRestriction.SORCERY_SPEED)
                .withSuspendsSourceFromHandX());
    }
}
