package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger: the controller may gain control of a target creature whose mana value
 * equals the triggering spell's mana value until end of turn.
 *
 * @param spellFilter which spells trigger this
 */
public record GainControlOfTargetCreatureByCastSpellManaValueEffect(CardPredicate spellFilter)
        implements CardEffect {
}
