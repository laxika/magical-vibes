package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "21")
public class BloodsproutTalisman extends Card {

    public BloodsproutTalisman() {
        addStaticEffect(new EntersTappedEffect());

        // {T}, Pay 1 life: Choose a nonland card in your hand. It perpetually gains
        // "This spell costs {1} less to cast."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1),
                        ChooseCardEffect.announced(Subjects.cardsInYourHand(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))), "a nonland card in your hand"),
                        new PerpetuallyChangeCastCostEffect(Subjects.cardsThisWay(StepKind.CHOOSE), -1)),
                "{T}, Pay 1 life: Choose a nonland card in your hand. It perpetually gains \"This spell costs {1} less to cast.\""
        ));
    }
}
