package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OwnLandsBecomeChosenTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "250")
public class ElsewhereFlask extends Card {

    public ElsewhereFlask() {
        // When this artifact enters, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        // Sacrifice this artifact: Choose a basic land type. Each land you control becomes
        // that type until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new OwnLandsBecomeChosenTypeUntilEndOfTurnEffect()),
                "Sacrifice Elsewhere Flask: Choose a basic land type. Each land you control becomes that type until end of turn."
        ));
    }
}
