package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "NPH", collectorNumber = "47")
@CardRegistration(set = "MM2", collectorNumber = "63")
@CardRegistration(set = "STA", collectorNumber = "21")
@CardRegistration(set = "NCC", collectorNumber = "235")
@CardRegistration(set = "M3C", collectorNumber = "194")
@CardRegistration(set = "OTC", collectorNumber = "117")
@CardRegistration(set = "C19", collectorNumber = "98")
@CardRegistration(set = "C16", collectorNumber = "99")
public class TezzeretsGambit extends Card {

    public TezzeretsGambit() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
