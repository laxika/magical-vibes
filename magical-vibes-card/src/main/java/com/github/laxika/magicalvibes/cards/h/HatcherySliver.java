package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CMM", collectorNumber = "741")
@CardRegistration(set = "CMM", collectorNumber = "771")
public class HatcherySliver extends Card {

    public HatcherySliver() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.REPLICATE, new CardSubtypePredicate(CardSubtype.SLIVER)));
    }
}
