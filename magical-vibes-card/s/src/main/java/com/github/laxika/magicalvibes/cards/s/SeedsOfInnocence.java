package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MIR", collectorNumber = "241")
public class SeedsOfInnocence extends Card {

    public SeedsOfInnocence() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsArtifactPredicate(),
                true,
                EachPermanentScope.ALL_PLAYERS,
                new EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect(),
                false));
    }
}
