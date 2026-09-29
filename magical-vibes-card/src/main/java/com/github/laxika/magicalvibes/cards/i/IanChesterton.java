package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "22")
@CardRegistration(set = "WHO", collectorNumber = "341")
public class IanChesterton extends Card {

    public IanChesterton() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.REPLICATE, new CardSubtypePredicate(CardSubtype.SAGA)));
    }
}
