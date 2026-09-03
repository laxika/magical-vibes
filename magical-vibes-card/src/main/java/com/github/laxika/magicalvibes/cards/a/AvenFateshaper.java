package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "69")
public class AvenFateshaper extends Card {

    public AvenFateshaper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new ReorderTopCardsOfLibraryEffect(4)),
                "{4}{U}: Look at the top four cards of your library, then put them back in any order."
        ));
    }
}
