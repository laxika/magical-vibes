package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "111")
public class HordelingOutburst extends Card {

    public HordelingOutburst() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
