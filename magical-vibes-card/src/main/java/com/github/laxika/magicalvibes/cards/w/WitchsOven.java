package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "237")
public class WitchsOven extends Card {

    public WitchsOven() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, true),
                        foodToken(),
                        new ConditionalEffect(new SpellXAtLeast(4), foodToken())
                ),
                "{T}, Sacrifice a creature: Create a Food token. If the sacrificed creature's toughness was 4 or greater, create two Food tokens instead."
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }
}
