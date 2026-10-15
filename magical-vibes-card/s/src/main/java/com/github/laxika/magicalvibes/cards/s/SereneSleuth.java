package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NoLongerGoadedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;

@CardRegistration(set = "MKC", collectorNumber = "14")
@CardRegistration(set = "MKC", collectorNumber = "325")
public class SereneSleuth extends Card {

    public SereneSleuth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                CreateTokenEffect.ofClueToken(new PermanentCount(
                        new PermanentIsGoadedPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new NoLongerGoadedEffect());
    }
}
