package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "100")
public class Mintstrosity extends Card {

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

    public Mintstrosity() {
        addEffect(EffectSlot.ON_DEATH, FOOD_TOKEN);
    }
}
