package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "165")
public class CinderCloud extends Card {

    public CinderCloud() {
        // Destroy target creature. If a white creature dies this way, Cinder Cloud deals damage to
        // that creature's controller equal to the creature's power.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        EventStat.POWER,
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        true));
    }
}
