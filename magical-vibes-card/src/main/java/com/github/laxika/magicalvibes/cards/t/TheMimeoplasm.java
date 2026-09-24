package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MimeoplasmCopyOnEnterEffect;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "136")
@CardRegistration(set = "SLD", collectorNumber = "1773")
@CardRegistration(set = "2X2", collectorNumber = "254")
@CardRegistration(set = "CMD", collectorNumber = "210")
public class TheMimeoplasm extends Card {

    public TheMimeoplasm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CopyCreatureCardInGraveyardOnEnterEffect(null, null, null, Set.of(), true));
    }
}
