package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceColoredManaWithWhiteUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;

@CardRegistration(set = "APC", collectorNumber = "10")
public class FalseDawn extends Card {

    public FalseDawn() {
        addSpellEffect(new ReplaceColoredManaWithWhiteUntilEndOfTurnEffect());
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(SpendManaAsAnyColorEffect.whiteMana()));
        addSpellEffect(new DrawCardEffect());
    }
}
