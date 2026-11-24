package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.YouAndOpponentChooseCardNamesOnEnterEffect;

@CardRegistration(set = "MIR", collectorNumber = "31")
public class NullChamber extends Card {

    public NullChamber() {
        addStaticEffect(new YouAndOpponentChooseCardNamesOnEnterEffect());
        addStaticEffect(SpellsWithChosenNameCantBeCastEffect.spellsAndLands());
    }
}
