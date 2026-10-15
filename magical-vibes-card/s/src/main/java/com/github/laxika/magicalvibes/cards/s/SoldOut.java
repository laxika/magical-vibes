package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "119")
public class SoldOut extends Card {

    public SoldOut() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                CreateTokenEffect.ofClueToken(1),
                ThenEffectRecipient.CONTROLLER,
                new PermanentDealtDamageThisTurnPredicate()));
    }
}
