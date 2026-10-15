package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "97")
public class FontOfIre extends Card {

    public FontOfIre() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetPlayerOrPlaneswalkerEffect(5)),
                "{3}{R}, Sacrifice this enchantment: It deals 5 damage to target player or planeswalker."
        ));
    }
}
