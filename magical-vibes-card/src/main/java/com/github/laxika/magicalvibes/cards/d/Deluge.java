package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;

import java.util.Set;

public class Deluge extends Card {

    public Deluge() {
        super("Deluge", CardType.INSTANT, "{2}{U}", CardColor.BLUE);

        setCardText("Tap all creatures without flying.");
        addEffect(EffectSlot.SPELL, new TapCreaturesEffect(Set.of(new WithoutKeywordTargetFilter(Keyword.FLYING))));
    }
}
