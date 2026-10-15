package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DesertManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "OTC", collectorNumber = "24")
@CardRegistration(set = "OTC", collectorNumber = "60")
public class CataclysmicProspecting extends Card {

    public CataclysmicProspecting() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), false));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTappedTreasureToken(new DesertManaSpentToCast()));
    }
}
