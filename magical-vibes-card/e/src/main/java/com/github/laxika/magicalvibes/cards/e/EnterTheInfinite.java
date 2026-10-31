package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.PutControllerCardFromHandOnTopOfLibraryEffect;

@CardRegistration(set = "GTC", collectorNumber = "34")
@CardRegistration(set = "PIO", collectorNumber = "365")
public class EnterTheInfinite extends Card {

    public EnterTheInfinite() {
        addSpellEffect(new DrawCardEffect(new CardsInLibrary(CountScope.CONTROLLER)));
        addSpellEffect(new PutControllerCardFromHandOnTopOfLibraryEffect());
        addSpellEffect(new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.UNTIL_NEXT_TURN));
    }
}
