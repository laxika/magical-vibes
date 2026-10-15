package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "78")
@CardRegistration(set = "M3C", collectorNumber = "130")
public class HorizonOfProgress extends Card {

    public HorizonOfProgress() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaOfTypeLandsCouldProduceEffect(
                                ManaColorLandScope.CONTROLLER, new PermanentIsLandPredicate())),
                "{T}, Pay 1 life: Add one mana of any type that a land you control could produce."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                        "Put a land card from your hand onto the battlefield tapped?")),
                "{3}, {T}: You may put a land card from your hand onto the battlefield tapped."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Sacrifice this land: Draw a card."
        ));
    }
}
