package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "2a")
@CardRegistration(set = "ALL", collectorNumber = "2b")
public class ErrandOfDuty extends Card {

    public ErrandOfDuty() {
        // Create a 1/1 white Knight creature token with banding.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Knight", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(Keyword.BANDING), Set.of()));
    }
}
