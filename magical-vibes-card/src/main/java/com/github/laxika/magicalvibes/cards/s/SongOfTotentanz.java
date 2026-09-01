package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "150")
public class SongOfTotentanz extends Card {

    public SongOfTotentanz() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Rat", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.RAT), Set.of(), Set.of())
                .withTokenEffects(Map.of(EffectSlot.STATIC, new CantBlockEffect())));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
    }
}
