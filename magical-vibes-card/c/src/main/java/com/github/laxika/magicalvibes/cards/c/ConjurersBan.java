package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantPlayChosenNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "108")
public class ConjurersBan extends Card {

    public ConjurersBan() {
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(CantPlayChosenNameUntilNextTurnEffect.nobodyCanPlay());
        addSpellEffect(new DrawCardEffect());
    }
}
