package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoHandAndDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "3")
public class MothlightProcessionist extends Card {

    public MothlightProcessionist() {
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardTypePredicate(CardType.ENCHANTMENT)));

        ConjureCardIntoHandAndDiscardAtNextEndStepEffect eerie =
                new ConjureCardIntoHandAndDiscardAtNextEndStepEffect("YDSK", "3");
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), eerie);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
    }
}
