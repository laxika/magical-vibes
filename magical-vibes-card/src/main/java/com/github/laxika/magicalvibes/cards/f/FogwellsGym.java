package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "754")
public class FogwellsGym extends Card {

    public FogwellsGym() {
        // {T}: Add {R}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.RED),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{T}: Add {R}. This land deals 1 damage to you."
        ));

        // {2}{R}, {T}, Discard a card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "{2}{R}, {T}, Discard a card: Draw a card."
        ));
    }
}
