package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerCreatureCardInGraveyardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "203")
public class SpiderSpawning extends Card {

    public SpiderSpawning() {
        addEffect(EffectSlot.SPELL, new CreateTokensPerCreatureCardInGraveyardEffect(
                "Spider", 1, 2, CardColor.GREEN, List.of(CardSubtype.SPIDER),
                Set.of(Keyword.REACH), Set.of(), false
        ));
        addCastingOption(new FlashbackCast("{6}{B}"));
    }
}
