package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

/**
 * Static replacement effect that doubles damage dealt by the sources {@code sources} names, optionally restricted by
 * an additional source predicate (CR 614.1a).
 *
 * @param additionalSourceFilter what else a permanent source must match, or {@code null}
 * @param sources                {@link Subjects#creaturesYouControl()} (Gratuitous Violence),
 *                               {@link Subjects#permanentsYouControl()} ("a [quality] source you control": Calamity
 *                               Bearer, Collective Inferno, Obosh), {@link Subjects#source()} (this permanent) or
 *                               {@link Subjects#attachedCreature()} ("equipped / enchanted creature", whoever controls
 *                               it)
 * @param spellSourceFilter      what a spell you control dealing damage itself must match to be doubled, judged on its
 *                               card (a spell's X counted in its mana value), or {@code null} when no spell is such a
 *                               source; only with {@link Subjects#permanentsYouControl()}
 * @param damage                 which damage is doubled
 */
public record DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter, PermanentSubject sources,
                                              CardPredicate spellSourceFilter, DamageScope damage)
        implements SourceDamageMultiplyingEffect {

    /** Which damage of a matching source is doubled. */
    public enum DamageScope {
        /** All damage it would deal. */
        ALL,
        /** Only combat damage, to any permanent or player. */
        COMBAT,
        /** Only combat damage to a player. */
        COMBAT_TO_PLAYERS
    }

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();
    private static final PermanentPredicate ANY_FILTER = new PermanentTruePredicate();
    private static final PermanentPredicate SELF_FILTER = new PermanentIsSourcePermanentPredicate();
    private static final PermanentPredicate HOST_FILTER = new PermanentAllOfPredicate(List.of(
            CREATURE_FILTER, new PermanentIsHostOfSourceAuraPredicate()));

    public DoubleDamageFromCreaturesEffect {
        if (!sources.equals(Subjects.creaturesYouControl()) && !sources.equals(Subjects.permanentsYouControl())
                && !sources.equals(Subjects.source()) && !sources.equals(Subjects.attachedCreature())) {
            throw new IllegalArgumentException("Unsupported damage sources: " + sources);
        }
        if (spellSourceFilter != null && !sources.equals(Subjects.permanentsYouControl())) {
            throw new IllegalArgumentException("Only sources you control include spells: " + sources);
        }
        if (damage == null) {
            damage = DamageScope.ALL;
        }
    }

    public DoubleDamageFromCreaturesEffect() {
        this(null, Subjects.creaturesYouControl());
    }

    public DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter) {
        this(additionalSourceFilter, Subjects.creaturesYouControl());
    }

    public DoubleDamageFromCreaturesEffect(PermanentSubject sources) {
        this(null, sources);
    }

    public DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter, PermanentSubject sources) {
        this(additionalSourceFilter, sources, null, DamageScope.ALL);
    }

    /**
     * "If a [quality] source you control would deal damage": permanents you control matching {@code permanentFilter}
     * and spells you control whose card matches {@code spellFilter}.
     */
    public static DoubleDamageFromCreaturesEffect fromSourcesYouControl(PermanentPredicate permanentFilter,
                                                                        CardPredicate spellFilter) {
        return new DoubleDamageFromCreaturesEffect(permanentFilter, Subjects.permanentsYouControl(), spellFilter,
                DamageScope.ALL);
    }

    /** This effect doubling only the {@code scope} damage of its sources. */
    public DoubleDamageFromCreaturesEffect onlyDoubling(DamageScope scope) {
        return new DoubleDamageFromCreaturesEffect(additionalSourceFilter, sources, spellSourceFilter, scope);
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        PermanentPredicate named = sources instanceof PermanentSubject.Source ? SELF_FILTER
                : sources instanceof PermanentSubject.Attached ? HOST_FILTER
                : sources.equals(Subjects.permanentsYouControl()) ? ANY_FILTER
                : CREATURE_FILTER;
        if (additionalSourceFilter == null) {
            return named;
        }
        return named == ANY_FILTER ? additionalSourceFilter
                : new PermanentAllOfPredicate(List.of(named, additionalSourceFilter));
    }

    @Override
    public boolean requiresSourceControllerMatch() {
        return !(sources instanceof PermanentSubject.Attached);
    }

    @Override
    public boolean appliesToNonCombatDamage() {
        return damage == DamageScope.ALL;
    }

    @Override
    public boolean appliesToCombatDamageTarget(Permanent target) {
        return damage != DamageScope.COMBAT_TO_PLAYERS || target == null;
    }
}
