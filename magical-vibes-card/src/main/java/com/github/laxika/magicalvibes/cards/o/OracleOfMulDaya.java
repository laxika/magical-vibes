package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "ZEN", collectorNumber = "172")
public class OracleOfMulDaya extends Card {

    public OracleOfMulDaya() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());
    }
}
