package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M15", collectorNumber = "47")
public class ChiefEngineer extends Card {

    public ChiefEngineer() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(Keyword.CONVOKE, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
