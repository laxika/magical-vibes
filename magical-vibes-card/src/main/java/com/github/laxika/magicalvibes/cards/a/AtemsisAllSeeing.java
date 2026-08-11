package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "46")
public class AtemsisAllSeeing extends Card {

    public AtemsisAllSeeing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "{2}{U}, {T}: Draw two cards, then discard a card."
        ));
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffect(),
                        "Reveal your hand?"
                ));
    }
}
