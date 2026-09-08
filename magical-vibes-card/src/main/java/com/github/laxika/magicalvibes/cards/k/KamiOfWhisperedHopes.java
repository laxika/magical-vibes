package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersToPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "196")
public class KamiOfWhisperedHopes extends Card {

    public KamiOfWhisperedHopes() {
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersToPermanentsEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new SourcePower())),
                "{T}: Add X mana of any one color, where X is this creature's power."
        ));
    }
}
