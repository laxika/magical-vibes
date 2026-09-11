package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardFaceDownInsteadOfDrawEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "88")
public class AsmodeusTheArchfiend extends Card {

    public AsmodeusTheArchfiend() {
        addEffect(EffectSlot.STATIC, new ExileTopCardFaceDownInsteadOfDrawEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}{B}",
                List.of(new DrawCardEffect(7)),
                "{B}{B}{B}: Draw seven cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect(),
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER)
                ),
                "{B}: Return all cards exiled with Asmodeus the Archfiend to their owner's hand and you lose that much life."
        ));
    }
}
