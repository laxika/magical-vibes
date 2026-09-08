package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSelfFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "ONS", collectorNumber = "37")
public class GustcloakSentinel extends Card {

    public GustcloakSentinel() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new RemoveSelfFromCombatEffect()
                ),
                "Untap Gustcloak Sentinel and remove it from combat?"
        ));
    }
}
