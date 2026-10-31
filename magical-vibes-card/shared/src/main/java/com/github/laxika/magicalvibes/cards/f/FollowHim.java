package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class FollowHim extends Card {

    public FollowHim() {
        addSpellEffect(CreateTokenEffect.ofClueToken(new XValue()));
    }
}
