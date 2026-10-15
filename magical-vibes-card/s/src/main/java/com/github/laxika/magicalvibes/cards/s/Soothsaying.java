package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "104")
public class Soothsaying extends Card {

    public Soothsaying() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new ShuffleLibraryEffect(false)),
                "{3}{U}{U}: Shuffle your library."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new ReorderTopCardsOfLibraryEffect(new XValue())),
                "{X}: Look at the top X cards of your library, then put them back in any order."
        ));
    }
}
