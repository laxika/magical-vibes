package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingPermanentWasNonlandCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAsNamedLandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "95")
public class PrincessYue extends Card {

    public PrincessYue() {
        addEffect(EffectSlot.ON_DEATH, new DyingPermanentWasNonlandCreatureConditionalEffect(
                new ReturnDyingCreatureToBattlefieldAsNamedLandEffect("Moon")));
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new ScryEffect(2)), "{T}: Scry 2."));
    }
}
