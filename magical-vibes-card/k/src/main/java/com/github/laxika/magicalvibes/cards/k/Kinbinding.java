package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "20")
@CardRegistration(set = "ECL", collectorNumber = "299")
@CardRegistration(set = "ECL", collectorNumber = "407")
public class Kinbinding extends Card {

    public Kinbinding() {
        var creaturesEntered = new CreaturesEnteredBattlefieldThisTurn(CountScope.CONTROLLER);
        addStaticEffect(new DynamicStaticBoostEffect(
                creaturesEntered, creaturesEntered, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new CreateTokenEffect(
                "Kithkin", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN)));
    }
}
