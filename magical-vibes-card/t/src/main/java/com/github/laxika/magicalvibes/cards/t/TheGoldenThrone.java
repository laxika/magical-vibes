package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "157")
public class TheGoldenThrone extends Card {

    public TheGoldenThrone() {
        // If you would lose the game, instead exile The Golden Throne and your life total becomes 1.
        addEffect(EffectSlot.STATIC, new ReplaceControllerLossWithExileAndLifeTotalEffect(1));

        // {T}, Sacrifice a creature: Add three mana in any combination of colors.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new AwardAnyColorManaEffect(3, true)),
                "{T}, Sacrifice a creature: Add three mana in any combination of colors."
        ));
    }
}
