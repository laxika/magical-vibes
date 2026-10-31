package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "111")
@CardRegistration(set = "EA1", collectorNumber = "9")
@CardRegistration(set = "Q06", collectorNumber = "7")
@CardRegistration(set = "TLE", collectorNumber = "35")
public class SearingBlood extends Card {

    public SearingBlood() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(2))
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER)));
    }
}
