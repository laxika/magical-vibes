package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "71")
public class YuanTiScaleshield extends Card {

    public YuanTiScaleshield() {
        addEffect(EffectSlot.SPELL,
                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.OWN_PERMANENTS));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentCastSpellThisTurn(new CardMaxManaValuePredicate(3)),
                new SeekLibraryEffect(1, new CardTypePredicate(CardType.CREATURE))));
    }
}
