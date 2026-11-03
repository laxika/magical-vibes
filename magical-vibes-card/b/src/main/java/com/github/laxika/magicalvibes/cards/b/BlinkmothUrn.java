package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "145")
@CardRegistration(set = "C18", collectorNumber = "197")
@CardRegistration(set = "C16", collectorNumber = "244")
public class BlinkmothUrn extends Card {

    public BlinkmothUrn() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceUntapped()),
                new AwardManaToActivePlayerEffect(
                        ManaColor.COLORLESS,
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
    }
}
