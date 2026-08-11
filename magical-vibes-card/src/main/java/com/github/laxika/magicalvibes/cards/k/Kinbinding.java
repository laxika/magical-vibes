package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "20")
public class Kinbinding extends Card {

    public Kinbinding() {
        var creaturesEntered = new CreaturesEnteredBattlefieldThisTurn(CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                creaturesEntered, creaturesEntered, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenEffect(
                "Kithkin", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN)));
    }
}
