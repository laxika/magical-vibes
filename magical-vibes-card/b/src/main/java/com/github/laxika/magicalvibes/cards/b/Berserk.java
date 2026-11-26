package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ME1", collectorNumber = "114")
@CardRegistration(set = "V09", collectorNumber = "2")
@CardRegistration(set = "VMA", collectorNumber = "197")
@CardRegistration(set = "SLD", collectorNumber = "1003")
@CardRegistration(set = "SLD", collectorNumber = "1738")
@CardRegistration(set = "SOA", collectorNumber = "50")
@CardRegistration(set = "2ED", collectorNumber = "186")
@CardRegistration(set = "LEA", collectorNumber = "185")
@CardRegistration(set = "LEB", collectorNumber = "186")
public class Berserk extends Card {

    public Berserk() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_COMBAT_DAMAGE);

        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(BoostEffect.target(new TargetPower(), new Fixed(0)))
                .addSpellEffect(RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endStep(ActivePlayer.ANY),
                        Subjects.target(),
                        new ConditionalEffect(new TriggeringPermanentMatches(new PermanentAttackedThisTurnPredicate()),
                                new DestroyTargetPermanentEffect(Subjects.triggering()))));
    }
}
