package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "YDFT", collectorNumber = "28")
public class WishGoodLuck extends Card {

    public WishGoodLuck() {
        addEffect(EffectSlot.SPELL, foodToken());
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTappedTreasureToken(1));
        addEffect(EffectSlot.SPELL, vehicleToken());
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }

    private static CreateTokenEffect vehicleToken() {
        return new CreateTokenEffect(
                CardType.ARTIFACT,
                1,
                "Vehicle",
                3,
                2,
                null,
                null,
                List.of(CardSubtype.VEHICLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                        "Crew 1"
                )),
                false,
                false,
                false,
                0,
                Set.of()
        );
    }
}
