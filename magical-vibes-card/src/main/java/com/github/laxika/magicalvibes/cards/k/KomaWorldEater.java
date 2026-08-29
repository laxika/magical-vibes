package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "121")
public class KomaWorldEater extends Card {

    public KomaWorldEater() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenEffect(4, "Koma's Coil", 3, 3, CardColor.BLUE,
                        List.of(CardSubtype.SERPENT), Set.of(), Set.of()));
    }
}
