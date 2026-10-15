package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "C20", collectorNumber = "44")
public class Mindleecher extends Card {

    public Mindleecher() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new ExileTopCardsToSourceEffect(1, true, false, LibraryScope.EACH_OPPONENT));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false, true));
    }
}
