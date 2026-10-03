package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "20")
@CardRegistration(set = "ONC", collectorNumber = "58")
public class GlisteningSphere extends Card {

    public GlisteningSphere() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "Corrupted — {T}: Add three mana of any one color. Activate only if an opponent has three or more poison counters."
        ).withActivationCondition(
                new OpponentPoisoned(3),
                "An opponent must have at least 3 poison counters"
        ));
    }
}
