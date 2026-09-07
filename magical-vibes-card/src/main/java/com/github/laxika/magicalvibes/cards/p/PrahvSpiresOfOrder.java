package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "177")
public class PrahvSpiresOfOrder extends Card {

    public PrahvSpiresOfOrder() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}{U}",
                List.of(PreventDamageFromChosenSourceEffect.allDamage(null, null)),
                "{4}{W}{U}, {T}: Prevent all damage a source of your choice would deal this turn."
        ));
    }
}
