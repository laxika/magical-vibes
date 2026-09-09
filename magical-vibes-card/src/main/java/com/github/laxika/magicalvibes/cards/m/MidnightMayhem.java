package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "222")
public class MidnightMayhem extends Card {

    public MidnightMayhem() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Gremlin", 1, 1, CardColor.RED, List.of(CardSubtype.GREMLIN), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.MENACE, Keyword.LIFELINK, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.GREMLIN)));
    }
}
