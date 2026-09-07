package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "ONS", collectorNumber = "280")
public class SerpentineBasilisk extends Card {

    public SerpentineBasilisk() {
        addMorph("{1}{G}{G}");

        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentIsSourcePermanentPredicate()));
    }
}
