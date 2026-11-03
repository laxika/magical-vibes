package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoHandAndDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "3")
public class MothlightProcessionist extends Card {

    public MothlightProcessionist() {
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardTypePredicate(CardType.ENCHANTMENT)));

        ConjureCardIntoHandAndDiscardAtNextEndStepEffect eerie =
                new ConjureCardIntoHandAndDiscardAtNextEndStepEffect("YDSK", "3");
        addTrigger(List.of(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                Triggers.youFullyUnlockRoom()), eerie);
    }
}
