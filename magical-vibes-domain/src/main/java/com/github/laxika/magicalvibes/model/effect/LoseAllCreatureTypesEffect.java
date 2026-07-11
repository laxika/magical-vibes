package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: creatures lose all creature types until end of turn. The
 * {@link GrantScope} selects who is affected: {@link GrantScope#TARGET} strips the single
 * targeted creature (e.g. Amoeboid Changeling's second ability), while
 * {@link GrantScope#TARGET_PLAYERS_CREATURES} strips every creature the targeted player
 * controls (e.g. Ego Erasure). Sets {@code Permanent.losesAllCreatureTypesUntilEndOfTurn},
 * which makes every creature subtype (base, transient, granted) read as absent and nullifies
 * the Changeling keyword's type grant. Cleared at end of turn by {@code resetModifiers()}.
 * The inverse "gains all creature types" is {@code GrantKeywordEffect(Keyword.CHANGELING, scope)}.
 */
public record LoseAllCreatureTypesEffect(GrantScope scope) implements CardEffect {

    /** Single targeted creature loses all creature types (Amoeboid Changeling). */
    public LoseAllCreatureTypesEffect() {
        this(GrantScope.TARGET);
    }

    @Override
    public boolean canTargetPermanent() {
        return scope == GrantScope.TARGET;
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == GrantScope.TARGET_PLAYERS_CREATURES;
    }
}
