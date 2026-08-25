package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinCopyTriggeringSpellOrDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "197")
public class BreechesTheBlastmaker extends Card {

    public BreechesTheBlastmaker() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentIsArtifactPredicate(),
                                new FlipCoinCopyTriggeringSpellOrDealDamageEffect(),
                                "an artifact"),
                        "Sacrifice an artifact?"))
        ));
    }
}
