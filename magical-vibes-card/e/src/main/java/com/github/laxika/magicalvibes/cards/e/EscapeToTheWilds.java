package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "ELD", collectorNumber = "189")
@CardRegistration(set = "ELD", collectorNumber = "379")
@CardRegistration(set = "SLD", collectorNumber = "1916")
@CardRegistration(set = "SLD", collectorNumber = "1921")
@CardRegistration(set = "MAR", collectorNumber = "89")
@CardRegistration(set = "OTC", collectorNumber = "223")
@CardRegistration(set = "EOC", collectorNumber = "115")
public class EscapeToTheWilds extends Card {

    public EscapeToTheWilds() {
        addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(5));
        addSpellEffect(new PlayAdditionalLandsEffect(1));
    }
}
