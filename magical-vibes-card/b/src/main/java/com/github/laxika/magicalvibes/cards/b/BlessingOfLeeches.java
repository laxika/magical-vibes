package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "62")
public class BlessingOfLeeches extends Card {

    public BlessingOfLeeches() {
        // Enchant creature
        target(TargetFilters.creature());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new RegenerateEffect()),
                "{0}: Regenerate enchanted creature."
        ));
    }
}
