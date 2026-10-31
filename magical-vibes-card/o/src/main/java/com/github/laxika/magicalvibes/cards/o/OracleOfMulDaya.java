package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "ZEN", collectorNumber = "172")
@CardRegistration(set = "2X2", collectorNumber = "154")
@CardRegistration(set = "OTC", collectorNumber = "200")
@CardRegistration(set = "EOC", collectorNumber = "102")
public class OracleOfMulDaya extends Card {

    public OracleOfMulDaya() {
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));
        addStaticEffect(new PlayWithTopCardRevealedEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
    }
}
