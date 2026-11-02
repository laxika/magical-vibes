package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExudeToxin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "90")
public class ScavengerRegentExudeToxin extends Card {

    public ScavengerRegentExudeToxin() {
        setBackFaceCard(new ExudeToxin());
        addCastingOption(new OmenCast());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessDiscardsEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ExudeToxin";
    }
}
