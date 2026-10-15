package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "YDSK", collectorNumber = "16")
public class HarrowingSwarm extends Card {

    public HarrowingSwarm() {
        addEffect(EffectSlot.SPELL, ManifestDreadEffect.forController());
        addEffect(EffectSlot.SPELL, new GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect(
                -2,
                new ConditionalEffect(new SourceIsCreature(),
                        new PutCountersOnSourceEffect(1, 1, 1))));
    }
}
