package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "17")
public class HauntingWind extends Card {

    public HauntingWind() {
        CardEffect artifactTapTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, artifactTapTrigger);
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, artifactTapTrigger);

        CardEffect artifactActivationTrigger = new TriggeringNonTapAbilityConditionalEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY, artifactActivationTrigger);
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_ABILITY, artifactActivationTrigger);
    }
}
