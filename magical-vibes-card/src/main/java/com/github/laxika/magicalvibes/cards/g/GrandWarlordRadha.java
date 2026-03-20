package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;

@CardRegistration(set = "DOM", collectorNumber = "195")
public class GrandWarlordRadha extends Card {

    public GrandWarlordRadha() {
        // Whenever one or more creatures you control attack, add that much mana
        // in any combination of {R} and/or {G}. Until end of turn, you don't lose
        // this mana as steps and phases end.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new AddManaPerAttackingCreatureEffect(ManaColor.RED, ManaColor.GREEN));
    }
}
