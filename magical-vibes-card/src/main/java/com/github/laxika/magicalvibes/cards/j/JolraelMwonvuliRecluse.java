package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "191")
public class JolraelMwonvuliRecluse extends Card {

    public JolraelMwonvuliRecluse() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new NthCardDrawTriggerEffect(2,
                new CreateTokenEffect("Cat", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.CAT), Set.of(), Set.of())));

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(new SetAllOwnCreaturesBasePowerToughnessEffect(cardsInHand, cardsInHand)),
                "{4}{G}{G}: Until end of turn, creatures you control have base power and toughness X/X, where X is the number of cards in your hand."
        ));
    }
}
