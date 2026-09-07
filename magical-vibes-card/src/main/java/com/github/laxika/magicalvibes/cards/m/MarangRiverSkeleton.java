package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "108")
public class MarangRiverSkeleton extends Card {

    public MarangRiverSkeleton() {
        addMorph("{3}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate Marang River Skeleton."
        ));
    }
}
