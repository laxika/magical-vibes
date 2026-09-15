package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLX", collectorNumber = "4")
public class ElmarUlvenwaldInformant extends Card {

    public ElmarUlvenwaldInformant() {
        // Whenever you cast your second spell each turn, untap target creature, then investigate.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        CreateTokenEffect.ofClueToken(1)
                ),
                TargetFilters.creature()
        ));
    }
}
