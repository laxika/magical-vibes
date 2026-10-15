package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSelfFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "22")
public class GustcloakCavalier extends Card {

    public GustcloakCavalier() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                "Tap target creature?"
        ));

        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new RemoveSelfFromCombatEffect()
                ),
                "Untap Gustcloak Cavalier and remove it from combat?"
        ));
    }
}
