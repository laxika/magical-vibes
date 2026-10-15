package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BindSpirit;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

@CardRegistration(set = "HBG", collectorNumber = "155")
public class GhostLantern extends Card {

    public GhostLantern() {
        setBackFaceCard(new BindSpirit());
        addCastingOption(new AdventureCast("{1}{B}"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BindSpirit";
    }
}
