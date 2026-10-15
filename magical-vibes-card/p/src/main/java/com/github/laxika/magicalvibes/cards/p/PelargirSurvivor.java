package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "182")
public class PelargirSurvivor extends Card {

    public PelargirSurvivor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.INSTANT_SORCERY_ONLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{U}",
                List.of(new MillEffect(3, MillRecipient.TARGET_PLAYER)),
                "{5}{U}, {T}: Target player mills three cards."
        ));
    }
}
