package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "125")
public class CryptRats extends Card {

    public CryptRats() {
        // {X}: This creature deals X damage to each creature and each player. (Pestilence on a body.)
        // The "spend only black mana on X" payment restriction is a flavor nuance the mana engine
        // does not model; the damage behavior is what matters.
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new MassDamageEffect(new XValue(), true)),
                "{X}: This creature deals X damage to each creature and each player. Spend only black mana on X."));
    }
}
