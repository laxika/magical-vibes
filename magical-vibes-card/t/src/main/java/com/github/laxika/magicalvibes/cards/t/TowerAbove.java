package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "131")
public class TowerAbove extends Card {

    public TowerAbove() {
        // Until end of turn, target creature gets +4/+4 and gains trample, wither, and
        // "When this creature attacks, target creature blocks it this turn if able."
        // The granted attack trigger is modeled as a temporary Triggers.attacks() ability that, when the
        // boosted creature attacks, forces a chosen creature to block it (MustBlockSourceEffect,
        // source snapshotted to the attacker in CombatAttackService).
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(4, 4))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.WITHER, Subjects.target()))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.attacks(), new MustBlockSourceEffect(null))));
    }
}
