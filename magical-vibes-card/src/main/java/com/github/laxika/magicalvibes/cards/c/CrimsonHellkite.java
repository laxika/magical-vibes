package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "178")
public class CrimsonHellkite extends Card {

    public CrimsonHellkite() {
        // Flying comes from Scryfall metadata. The "spend only red mana on X" payment restriction
        // is a flavor nuance the mana engine does not model; the damage behavior is what matters.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new DealDamageToTargetCreatureEffect(new XValue())),
                "{X}, {T}: This creature deals X damage to target creature. Spend only red mana on X."));
    }
}
