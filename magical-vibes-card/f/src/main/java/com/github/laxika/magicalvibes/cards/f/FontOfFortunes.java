package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "38")
public class FontOfFortunes extends Card {

    public FontOfFortunes() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "Sacrifice Font of Fortunes: Draw two cards."
        ));
    }
}
