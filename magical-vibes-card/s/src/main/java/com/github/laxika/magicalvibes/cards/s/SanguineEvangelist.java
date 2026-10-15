package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "34")
@CardRegistration(set = "LCI", collectorNumber = "356")
public class SanguineEvangelist extends Card {

    public SanguineEvangelist() {
        CreateTokenEffect bat = new CreateTokenEffect(
                "Bat", 1, 1, CardColor.BLACK, List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, bat);
        addEffect(EffectSlot.ON_DEATH, bat);
    }
}
