package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerDiscardsAndLosesLifeIfLandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "14")
@CardRegistration(set = "OPCA", collectorNumber = "26")
public class FurnaceLayer extends Card {

    public FurnaceLayer() {
        RandomPlayerDiscardsAndLosesLifeIfLandEffect discardEffect =
                new RandomPlayerDiscardsAndLosesLifeIfLandEffect();
        addTrigger(Triggers.planeswalkToThis(), discardEffect);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), discardEffect);

        target(TargetFilters.nonlandPermanent()).addTrigger(Triggers.chaosEnsues(),
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target nonland permanent?"));
    }
}
