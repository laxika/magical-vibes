package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "271")
@CardRegistration(set = "EMN", collectorNumber = "200")
@CardRegistration(set = "SIR", collectorNumber = "259")
public class StitchersGraft extends Card {

    public StitchersGraft() {
        addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.attacks(), new SkipNextUntapEffect(TapUntapScope.TRIGGERING));
        addTrigger(Triggers.becomesUnattachedFromPermanent(), new SacrificeTriggeringPermanentEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
