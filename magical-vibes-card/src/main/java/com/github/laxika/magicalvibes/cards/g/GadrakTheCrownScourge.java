package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.NontokenCreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "M21", collectorNumber = "146")
public class GadrakTheCrownScourge extends Card {

    public GadrakTheCrownScourge() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsPermanentCount(4, new PermanentIsArtifactPredicate()),
                "you control four or more artifacts"
        ));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.ofTreasureToken(
                        new NontokenCreatureDeathsThisTurn(CountScope.CONTROLLER)));
    }
}
