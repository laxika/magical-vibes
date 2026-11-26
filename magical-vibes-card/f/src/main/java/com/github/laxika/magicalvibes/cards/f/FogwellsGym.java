package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

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
                        DealDamageEffect.to(Subjects.you(), 1)
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
