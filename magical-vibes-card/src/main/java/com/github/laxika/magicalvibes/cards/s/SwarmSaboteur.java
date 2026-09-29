package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;

@CardRegistration(set = "YNEO", collectorNumber = "13")
public class SwarmSaboteur extends Card {

    public SwarmSaboteur() {
        addNinjutsu("{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConjureCardToHandEffect("Virus Beetle"));
    }
}
