package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SLD", collectorNumber = "1824")
public class DelayedBlastFireball extends Card {

    public DelayedBlastFireball() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.EXILE),
                damage(2),
                damage(5)
        ));
        addCastingOption(new ForetellCast("{4}{R}{R}"));
    }

    private static SequenceEffect damage(int amount) {
        return SequenceEffect.of(
                new DealDamageToPlayersEffect(amount, DamageRecipient.EACH_OPPONENT),
                new DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect(amount, false)
        );
    }
}
