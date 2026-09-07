package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "56")
public class KasminaEnigmaticMentor extends Card {

    public KasminaEnigmaticMentor() {
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )), 2, false));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new CreateTokenEffect("Wizard", 2, 2, CardColor.BLUE,
                                List.of(CardSubtype.WIZARD), Set.of(), Set.of()),
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "-2: Create a 2/2 blue Wizard creature token. Draw a card, then discard a card."
        ));
    }
}
