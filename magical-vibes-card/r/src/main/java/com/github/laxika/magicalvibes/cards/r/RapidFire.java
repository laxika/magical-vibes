package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "32")
public class RapidFire extends Card {

    public RapidFire() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_BLOCKERS_DECLARED);
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(
                        Keyword.FIRST_STRIKE, Subjects.target()))
                .addSpellEffect(new GrantEffectToTargetEffect(
                        new TriggeredAbility(Triggers.becomesBlocked(),
                        new RampageEffect(2)),
                        Duration.UNTIL_END_OF_TURN));
    }
}
