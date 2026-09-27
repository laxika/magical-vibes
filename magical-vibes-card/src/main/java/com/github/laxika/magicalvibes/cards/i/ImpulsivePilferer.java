package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "234")
public class ImpulsivePilferer extends Card {

    public ImpulsivePilferer() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofTreasureToken(1));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {3}{R}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
