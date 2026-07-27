package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;


@CardRegistration(set = "AKH", collectorNumber = "35")
public class TrueheartDuelist extends Card {

    public TrueheartDuelist() {
        // This creature can block an additional creature each combat.
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));

        // Embalm {2}{W} ({2}{W}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Human Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{2}{W}", "Human Warrior");
    }
}
