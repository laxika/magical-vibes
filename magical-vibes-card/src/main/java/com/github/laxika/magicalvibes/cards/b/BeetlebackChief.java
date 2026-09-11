package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PC2", collectorNumber = "40")
@CardRegistration(set = "DDN", collectorNumber = "14")
public class BeetlebackChief extends Card {

    public BeetlebackChief() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
