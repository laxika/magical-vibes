package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "947")
@CardRegistration(set = "SLD", collectorNumber = "2432")
public class GrandCrescendo extends Card {

    public GrandCrescendo() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Citizen", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN), Set.of(), Set.of(),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of()));
        addEffect(EffectSlot.SPELL,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
    }
}
