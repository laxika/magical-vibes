package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SharedFateDrawReplacementEffect;

@CardRegistration(set = "MRD", collectorNumber = "49")
public class SharedFate extends Card {

    public SharedFate() {
        addEffect(EffectSlot.STATIC, new SharedFateDrawReplacementEffect());
        addEffect(EffectSlot.STATIC,
                new AllowCastFromCardsExiledWithSourceEffect(false, ExileAccessScope.EXILER));
    }
}
