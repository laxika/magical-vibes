package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "4ED", collectorNumber = "312")
@CardRegistration(set = "ATQ", collectorNumber = "48")
@CardRegistration(set = "ME1", collectorNumber = "155")
public class CursedRack extends Card {

    public CursedRack() {
        // "As this artifact enters, choose an opponent. The chosen player's maximum hand size is four."
        addStaticEffect(new ChooseOpponentOnEnterEffect());
        addStaticEffect(SetControllerMaximumHandSizeEffect.set(Subjects.chosenPlayer(), new Fixed(4)));
    }
}
