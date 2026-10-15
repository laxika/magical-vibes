package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "654")
public class DoctorDoomUnrivaled extends Card {

    public DoctorDoomUnrivaled() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1),
                        new ConditionalEffect(
                                new NotCondition(new CardsInLibraryAtLeast(1)),
                                new WinGameEffect())
                ),
                "{T}: You draw a card and lose 1 life. Then if your library has no cards in it, you win the game."
        ));
    }
}
