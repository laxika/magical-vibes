package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "23")
public class CrystalSeer extends Card {

    public CrystalSeer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new ReturnSelfToHandCost()),
                "{4}{U}: Return this creature to its owner's hand."
        ));
    }
}
