package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "23")
@CardRegistration(set = "OTC", collectorNumber = "59")
public class ThievingVarmint extends Card {

    public ThievingVarmint() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardRestrictedManaOfColorsEffect(
                                ManaColor.COLORS, 2, new ManaRestriction.NonOwnedSpells(), true)),
                "{T}, Pay 1 life: Add two mana of any one color. Spend this mana only to cast spells you don't own."
        ));
    }
}
