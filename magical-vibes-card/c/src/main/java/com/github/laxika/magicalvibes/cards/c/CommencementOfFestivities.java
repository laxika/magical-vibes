package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "KLD", collectorNumber = "148")
@CardRegistration(set = "KLR", collectorNumber = "157")
public class CommencementOfFestivities extends Card {

    public CommencementOfFestivities() {
        addSpellEffect(PreventDamageEffect.allCombatToPlayers());
    }
}
