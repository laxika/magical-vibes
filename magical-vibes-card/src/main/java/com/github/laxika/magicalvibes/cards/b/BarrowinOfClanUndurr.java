package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "218")
public class BarrowinOfClanUndurr extends Card {

    public BarrowinOfClanUndurr() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(new ControllerHasCompletedDungeon(),
                new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3))),
                        1)));
    }
}
