package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "133")
@CardRegistration(set = "DRB", collectorNumber = "4")
@CardRegistration(set = "4ED", collectorNumber = "186")
@CardRegistration(set = "SUM", collectorNumber = "143")
@CardRegistration(set = "TSB", collectorNumber = "59")
public class DragonWhelp extends Card {

    public DragonWhelp() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(
                new BoostSelfEffect(1, 0),
                new ConditionalEffect(new ActivationCount(4, 0), new SacrificeSelfAtEndStepEffect())),
                "{R}: Dragon Whelp gets +1/+0 until end of turn. If this ability has been activated four or more times this turn, sacrifice Dragon Whelp at the beginning of the next end step."));
    }
}
