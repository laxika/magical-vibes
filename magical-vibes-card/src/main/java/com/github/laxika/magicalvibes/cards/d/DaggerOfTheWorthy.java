package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "HOU", collectorNumber = "160")
public class DaggerOfTheWorthy extends Card {

    public DaggerOfTheWorthy() {
        // Equipped creature gets +2/+0
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Afflict 1 — whenever equipped creature becomes blocked, defending player loses 1 life
        // (once per becoming blocked, not per blocker). Modeled on the Equipment like Infiltration Lens.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
