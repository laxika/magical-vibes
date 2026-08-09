package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "230")
public class DesecratedTomb extends Card {

    public DesecratedTomb() {
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
                new CreateTokenEffect("Bat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of()));
    }
}
