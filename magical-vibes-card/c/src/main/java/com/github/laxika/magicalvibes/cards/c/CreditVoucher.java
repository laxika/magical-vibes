package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleAnyNumberCardsFromHandIntoLibraryThenDrawThatManyEffect;
import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "289")
public class CreditVoucher extends Card {

    public CreditVoucher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new ShuffleAnyNumberCardsFromHandIntoLibraryThenDrawThatManyEffect()),
                "{2}, {T}, Sacrifice this artifact: Shuffle any number of cards from your hand into your library, then draw that many cards."));
    }
}
