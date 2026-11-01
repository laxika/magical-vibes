package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeathlyRide;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "92")
public class FellHorseman extends Card {

    public FellHorseman() {
        setBackFaceCard(new DeathlyRide());
        addCastingOption(new AdventureCast("{1}{B}"));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DeathlyRide";
    }
}
