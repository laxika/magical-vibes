package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersForArtifactManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "38")
public class CoinOfMastery extends Card {

    public CoinOfMastery() {
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersForArtifactManaEffect(
                new PermanentIsCreaturePredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{T}: Create a Treasure token."
        ));
    }
}
