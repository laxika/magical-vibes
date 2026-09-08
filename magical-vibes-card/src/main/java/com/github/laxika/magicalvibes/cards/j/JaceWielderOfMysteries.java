package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "54")
public class JaceWielderOfMysteries extends Card {

    public JaceWielderOfMysteries() {
        addEffect(EffectSlot.STATIC, new WinGameOnEmptyLibraryDrawEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new MillEffect(2, MillRecipient.TARGET_PLAYER),
                        new DrawCardEffect(1)
                ),
                "+1: Target player mills two cards. Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new DrawCardEffect(7),
                        new ConditionalEffect(
                                new NotCondition(new CardsInLibraryAtLeast(1)),
                                new WinGameEffect()
                        )
                ),
                "−8: Draw seven cards. Then if your library has no cards in it, you win the game."
        ));
    }
}
