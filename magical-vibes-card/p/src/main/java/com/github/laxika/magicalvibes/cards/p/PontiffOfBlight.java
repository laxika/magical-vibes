package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "27")
@CardRegistration(set = "SLD", collectorNumber = "848")
@CardRegistration(set = "GK2", collectorNumber = "33")
@CardRegistration(set = "C14", collectorNumber = "155")
@CardRegistration(set = "AFC", collectorNumber = "108")
public class PontiffOfBlight extends Card {

    public PontiffOfBlight() {
        addTrigger(Triggers.youCastSpell().anySpell(), extort());
        // "Other creatures you control have extort" — OWN_CREATURES excludes the source, which
        // already has its own printed extort above. Each instance triggers separately.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.youCastSpell().anySpell(), extort()), Subjects.otherCreaturesYouControl()));
    }

    private static UnlessPaysEffect extort() {
        return UnlessPaysEffect.youMayPay("{W/B}", new LoseLifeEffect(1, Subjects.eachOpponent(), true))
                .withPrompt("Pay {W/B} to extort?");
    }
}
