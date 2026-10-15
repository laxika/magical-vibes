package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;

@CardRegistration(set = "NCC", collectorNumber = "93")
@CardRegistration(set = "NCC", collectorNumber = "111")
public class ThreefoldSignal extends Card {

    public ThreefoldSignal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.REPLICATE, 3, new CardHasExactlyNColorsPredicate(3)));
    }
}
