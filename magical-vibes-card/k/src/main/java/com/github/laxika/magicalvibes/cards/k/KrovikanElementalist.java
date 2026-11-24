package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "139")
public class KrovikanElementalist extends Card {

    public KrovikanElementalist() {
        // {2}{R}: Target creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{R}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{2}{R}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()
        ));

        // {U}{U}: Target creature you control gains flying until end of turn.
        // Sacrifice it at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                false, "{U}{U}",
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.target()),
                        RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endStep(ActivePlayer.ANY),
                                Subjects.target(),
                                SacrificeSelfEffect.ofTriggeringPermanent())
                ),
                "{U}{U}: Target creature you control gains flying until end of turn. "
                        + "Sacrifice it at the beginning of the next end step.",
                TargetFilters.creatureYouControl()
        ));
    }
}
