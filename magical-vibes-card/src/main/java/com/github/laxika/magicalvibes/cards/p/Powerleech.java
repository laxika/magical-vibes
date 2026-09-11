package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "34")
public class Powerleech extends Card {

    public Powerleech() {
        // Whenever an artifact an opponent controls becomes tapped, you gain 1 life.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new GainLifeEffect(1)));

        // Whenever an opponent activates an artifact's ability without {T} in its activation cost,
        // you gain 1 life.
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_ABILITY, new TriggeringNonTapAbilityConditionalEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new GainLifeEffect(1))));
    }
}
