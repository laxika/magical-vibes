package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "44")
public class Mindleecher extends Card {

    public Mindleecher() {
        addTrigger(Triggers.mutates(),
                new ExileTopCardsToSourceEffect(1, true, false, LibraryScope.EACH_OPPONENT,
                        false, null, false, true, true));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false, true));
    }
}
