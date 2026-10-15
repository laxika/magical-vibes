package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellsAndLandsWithChosenNamesCantBePlayedEffect;
import com.github.laxika.magicalvibes.model.effect.YouAndOpponentChooseCardNamesOnEnterEffect;

@CardRegistration(set = "MIR", collectorNumber = "31")
public class NullChamber extends Card {

    public NullChamber() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new YouAndOpponentChooseCardNamesOnEnterEffect());
        addEffect(EffectSlot.STATIC, new SpellsAndLandsWithChosenNamesCantBePlayedEffect());
    }
}
