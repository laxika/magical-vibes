package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Static effect for an Aura attached to an Equipment that grants keywords to that Equipment's
 * equipped creature.
 */
public record GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect(Set<Keyword> keywords)
        implements KeywordGrantingEffect {

    public GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect(Keyword keyword) {
        this(Set.of(keyword));
    }

    @Override
    public PermanentSubject subject() {
        return Subjects.attachedCreature();
    }

    @Override
    public com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter() {
        return null;
    }
}
