package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "53")
@CardRegistration(set = "MSC", collectorNumber = "364")
public class KillmongerRuthlessUsurper extends Card {

    public KillmongerRuthlessUsurper() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.DEFENDING_PLAYER),
                new Fixed(0)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(),
                        SacrificeRecipient.TARGET_PLAYER),
                CreateTokenEffect.ofTreasureToken(1)));
    }
}
