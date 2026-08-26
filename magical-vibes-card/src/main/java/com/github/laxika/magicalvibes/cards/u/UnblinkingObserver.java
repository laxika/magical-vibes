package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "82")
public class UnblinkingObserver extends Card {

    public UnblinkingObserver() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 1, new ManaRestriction.DisturbOrInstantSorcery())),
                "{T}: Add {U}. Spend this mana only to pay a disturb cost or cast an instant or sorcery spell."
        ));
    }
}
