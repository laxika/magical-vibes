package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "WHO", collectorNumber = "26")
@CardRegistration(set = "WHO", collectorNumber = "344")
public class PeriBrown extends Card {

    public PeriBrown() {
        // The first historic spell you cast each turn has convoke.
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
                Keyword.CONVOKE, new CardIsHistoricPredicate()));
    }
}
