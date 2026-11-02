package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSelfFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "34")
@CardRegistration(set = "VMA", collectorNumber = "30")
@CardRegistration(set = "DDO", collectorNumber = "10")
public class GustcloakHarrier extends Card {

    public GustcloakHarrier() {
        addTrigger(Triggers.becomesBlocked(), new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new RemoveSelfFromCombatEffect()
                ),
                "Untap Gustcloak Harrier and remove it from combat?"
        ));
    }
}
