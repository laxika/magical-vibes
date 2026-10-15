package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "90")
public class AgadeemsAwakening extends Card {

    public AgadeemsAwakening() {
        setBackFaceCard(new AgadeemTheUndercrypt());
        setModalDoubleFaced(true);
        setMultiTargetConstraint(MultiTargetConstraint.DIFFERENT_MANA_VALUES);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Agadeem's Awakening", List.of(
                        new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardMaxManaValueXPredicate())),
                                new Sum(new Fixed(1), new XValue()))
                )),
                new ChooseOneEffect.ChooseOneOption("Agadeem, the Undercrypt", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "AgadeemTheUndercrypt";
    }
}
