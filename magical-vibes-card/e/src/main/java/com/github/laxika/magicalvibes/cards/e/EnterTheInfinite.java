package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;

@CardRegistration(set = "GTC", collectorNumber = "34")
@CardRegistration(set = "PIO", collectorNumber = "365")
public class EnterTheInfinite extends Card {

    public EnterTheInfinite() {
        addSpellEffect(new DrawCardEffect(new CardsInLibrary(CountScope.CONTROLLER)));
        addSpellEffect(TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(1, HandToLibraryPlacement.TOP));
        addSpellEffect(new GrantNoMaximumHandSizeEffect(Duration.UNTIL_YOUR_NEXT_TURN));
    }
}
