package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "226")
public class KrydleOfBaldursGate extends Card {

    public KrydleOfBaldursGate() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new MillEffect(1, MillRecipient.TARGET_PLAYER),
                new GainLifeEffect(1),
                new ScryEffect(1)));

        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MayPayManaEffect("{2}", new MakeCreatureUnblockableEffect(),
                        "Pay {2} to make target creature unable to be blocked?"));
    }
}
