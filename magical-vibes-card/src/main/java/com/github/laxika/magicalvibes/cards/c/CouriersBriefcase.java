package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "142")
public class CouriersBriefcase extends Card {

    public CouriersBriefcase() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Citizen", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}{B}{R}{G}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(3)),
                "{W}{U}{B}{R}{G}, {T}, Sacrifice this artifact: Draw three cards."
        ));
    }
}
