package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "BRC", collectorNumber = "23")
@CardRegistration(set = "BRC", collectorNumber = "43")
public class SardianAvenger extends Card {

    public SardianAvenger() {
        // Whenever this creature attacks, it gets +X/+0 until end of turn, where X is the number of artifacts your opponents control.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS),
                new Fixed(0)));

        // Whenever an artifact an opponent controls is put into a graveyard from the battlefield, this creature deals 1 damage to that player.
        addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
