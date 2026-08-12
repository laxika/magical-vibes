package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "191")
public class PonybackBrigade extends Card {

    public PonybackBrigade() {
        CreateTokenEffect goblins = new CreateTokenEffect(
                3, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, goblins);
        addMorph("{2}{R}{W}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, goblins);
    }
}
