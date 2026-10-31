package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

public class TailTheSuspect extends Card {

    public TailTheSuspect() {
        addSpellEffect(CreateTokenEffect.ofClueToken(1));
        addSpellEffect(new PlayAdditionalLandsEffect(1));
    }
}
