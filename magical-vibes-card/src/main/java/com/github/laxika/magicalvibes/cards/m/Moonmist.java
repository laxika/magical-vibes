package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageExceptBySubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "195")
public class Moonmist extends Card {

    public Moonmist() {
        // Transform all Humans.
        addEffect(EffectSlot.SPELL, new TransformAllEffect(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));

        // Prevent all combat damage that would be dealt this turn by creatures other than Werewolves and Wolves.
        addEffect(EffectSlot.SPELL, new PreventCombatDamageExceptBySubtypesEffect(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WEREWOLF, CardSubtype.WOLF))));
    }
}
