package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "160")
public class FinaleOfDevastation extends Card {

    public FinaleOfDevastation() {
        addEffect(EffectSlot.SPELL, new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValueXPredicate()))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(10),
                new BoostAllOwnCreaturesEffect(new XValue(), new XValue())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(10),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)));
    }
}
