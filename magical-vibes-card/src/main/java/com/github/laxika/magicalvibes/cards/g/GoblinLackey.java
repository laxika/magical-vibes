package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "190")
public class GoblinLackey extends Card {

    public GoblinLackey() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MayEffect(
                new PutCardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardSubtypePredicate(CardSubtype.GOBLIN))),
                        "Goblin permanent"),
                "Put a Goblin permanent card from your hand onto the battlefield?"));
    }
}
