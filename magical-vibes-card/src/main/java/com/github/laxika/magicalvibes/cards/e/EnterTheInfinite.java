package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutControllerCardFromHandOnTopOfLibraryEffect;

@CardRegistration(set = "GTC", collectorNumber = "34")
public class EnterTheInfinite extends Card {

    public EnterTheInfinite() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardsInLibrary(CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new PutControllerCardFromHandOnTopOfLibraryEffect());
        addEffect(EffectSlot.SPELL, new GrantNoMaximumHandSizeUntilNextTurnEffect());
    }
}
