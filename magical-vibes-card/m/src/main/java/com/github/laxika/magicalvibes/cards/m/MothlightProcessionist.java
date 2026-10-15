package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoHandAndDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YDSK", collectorNumber = "3")
public class MothlightProcessionist extends Card {

    public MothlightProcessionist() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardTypePredicate(CardType.ENCHANTMENT)));

        ConjureCardIntoHandAndDiscardAtNextEndStepEffect eerie =
                new ConjureCardIntoHandAndDiscardAtNextEndStepEffect("YDSK", "3");
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, eerie);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
    }
}
