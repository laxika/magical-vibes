package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "17")
@CardRegistration(set = "PIP", collectorNumber = "327")
@CardRegistration(set = "PIP", collectorNumber = "545")
@CardRegistration(set = "PIP", collectorNumber = "855")
public class Idolized extends Card {

    public Idolized() {
        PermanentCount nonlandPermanents = new PermanentCount(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), CountScope.CONTROLLER);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                        new ConditionalEffect(new AttacksAlone(),
                                new BoostSelfEffect(nonlandPermanents, nonlandPermanents)),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
