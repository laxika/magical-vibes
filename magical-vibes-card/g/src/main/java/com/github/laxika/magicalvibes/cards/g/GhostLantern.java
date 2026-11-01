package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BindSpirit;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "155")
public class GhostLantern extends Card {

    public GhostLantern() {
        setBackFaceCard(new BindSpirit());
        addCastingOption(new AdventureCast("{1}{B}"));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BindSpirit";
    }
}
