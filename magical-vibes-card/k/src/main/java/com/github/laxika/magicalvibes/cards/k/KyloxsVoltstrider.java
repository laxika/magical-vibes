package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsExiledWithSourceEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "215")
@CardRegistration(set = "MKM", collectorNumber = "417")
public class KyloxsVoltstrider extends Card {

    public KyloxsVoltstrider() {
        addEffect(EffectSlot.ON_ATTACK, new MayCastInstantOrSorceryCardsExiledWithSourceEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CollectEvidenceCost(6), AnimatePermanentsEffect.crew()),
                "Collect evidence 6: This Vehicle becomes an artifact creature until end of turn."));
    }
}
