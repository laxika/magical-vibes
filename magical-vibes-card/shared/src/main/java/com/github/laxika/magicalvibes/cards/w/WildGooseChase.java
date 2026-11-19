package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class WildGooseChase extends Card {

    public WildGooseChase() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, Subjects.you()));
        addSpellEffect(CreateTokenEffect.ofFoodToken(1));
    }
}
