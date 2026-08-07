package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "202")
public class UndercityTroll extends Card {

    public UndercityTroll() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // {2}{G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new RegenerateEffect()),
                "{2}{G}: Regenerate this creature."));
    }
}
