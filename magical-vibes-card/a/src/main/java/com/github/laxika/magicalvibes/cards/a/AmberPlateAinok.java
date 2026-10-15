package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "YTDM", collectorNumber = "15")
public class AmberPlateAinok extends Card {

    public AmberPlateAinok() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(new SourceIsTapped(), new EndureEffect(1)));
    }
}
