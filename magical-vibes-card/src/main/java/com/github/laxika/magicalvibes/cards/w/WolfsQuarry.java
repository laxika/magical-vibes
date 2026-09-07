package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "184")
public class WolfsQuarry extends Card {

    private static final CreateTokenEffect FOOD_TOKEN = CreateTokenEffect.ofArtifactToken(
            1,
            "Food",
            List.of(CardSubtype.FOOD),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                    "{2}, {T}, Sacrifice this token: You gain 3 life."
            )));

    public WolfsQuarry() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                3,
                "Boar",
                1,
                1,
                CardColor.GREEN,
                null,
                List.of(CardSubtype.BOAR),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_DEATH, FOOD_TOKEN),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
