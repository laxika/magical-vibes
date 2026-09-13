package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ANA", collectorNumber = "40")
@CardRegistration(set = "XANA", collectorNumber = "40")
@CardRegistration(set = "ANB", collectorNumber = "70")
public class GoblinGangLeader extends Card {

    public GoblinGangLeader() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
