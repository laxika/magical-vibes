package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AtzalCaveOfEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "160")
public class JourneyToEternity extends Card {

    public JourneyToEternity() {
        setBackFaceCard(new AtzalCaveOfEternity());

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AtzalCaveOfEternity";
    }
}
