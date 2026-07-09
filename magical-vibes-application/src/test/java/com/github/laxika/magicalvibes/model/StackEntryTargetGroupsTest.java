package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StackEntry#targetsForGroup} and {@link StackEntry#targetsForEffect}:
 * slicing the flat target list by the card's {@code target(...)} declarations.
 */
class StackEntryTargetGroupsTest {

    private static final UUID CONTROLLER = UUID.randomUUID();

    private StackEntry spellEntry(Card card, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, CONTROLLER, "test", List.of(), 0, targetIds);
    }

    @Test
    @DisplayName("Two single-target groups slice into one target each")
    void twoSingleTargetGroups() {
        Card card = new Card();
        CardEffect first = new BoostTargetCreatureEffect(1, 1);
        CardEffect second = new BoostTargetCreatureEffect(-1, -1);
        card.target(1, 1).addEffect(EffectSlot.SPELL, first);
        card.target(1, 1).addEffect(EffectSlot.SPELL, second);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        StackEntry entry = spellEntry(card, List.of(a, b));

        assertThat(entry.targetsForGroup(0)).containsExactly(a);
        assertThat(entry.targetsForGroup(1)).containsExactly(b);
        assertThat(entry.targetsForEffect(first)).containsExactly(a);
        assertThat(entry.targetsForEffect(second)).containsExactly(b);
    }

    @Test
    @DisplayName("A single 'up to N' group receives every chosen target")
    void singleUpToNGroup() {
        Card card = new Card();
        CardEffect effect = new BoostTargetCreatureEffect(2, 2);
        card.target(1, 2).addEffect(EffectSlot.SPELL, effect);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        StackEntry entry = spellEntry(card, List.of(a, b));

        assertThat(entry.targetsForEffect(effect)).containsExactly(a, b);
    }

    @Test
    @DisplayName("Trailing variable-count group takes the remaining targets; empty when omitted")
    void fixedGroupThenVariableGroup() {
        Card card = new Card();
        CardEffect first = new BoostTargetCreatureEffect(1, 1);
        CardEffect rest = new BoostTargetCreatureEffect(0, 0);
        card.target(1, 1).addEffect(EffectSlot.SPELL, first);
        card.target(0, 2).addEffect(EffectSlot.SPELL, rest);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        StackEntry full = spellEntry(card, List.of(a, b, c));
        assertThat(full.targetsForEffect(first)).containsExactly(a);
        assertThat(full.targetsForEffect(rest)).containsExactly(b, c);

        StackEntry partial = spellEntry(card, List.of(a));
        assertThat(partial.targetsForEffect(first)).containsExactly(a);
        assertThat(partial.targetsForEffect(rest)).isEmpty();
    }

    @Test
    @DisplayName("An effect not bound to any group keeps the whole flat target list")
    void unboundEffectKeepsFlatList() {
        Card card = new Card();
        CardEffect unbound = new BoostTargetCreatureEffect(1, 1);
        card.addEffect(EffectSlot.SPELL, unbound);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        StackEntry entry = spellEntry(card, List.of(a, b));

        assertThat(entry.targetsForEffect(unbound)).containsExactly(a, b);
    }

    @Test
    @DisplayName("Without target() declarations the flat list is positional by group (abilities)")
    void positionalFallbackWithoutDeclarations() {
        Card card = new Card();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        StackEntry entry = spellEntry(card, List.of(a, b));

        assertThat(entry.targetsForGroup(0)).containsExactly(a);
        assertThat(entry.targetsForGroup(1)).containsExactly(b);
        assertThat(entry.targetsForGroup(2)).isEmpty();
    }

    @Test
    @DisplayName("Bound effect on a single-target cast resolves against the lone targetId")
    void boundEffectFallsBackToSingleTargetId() {
        Card card = new Card();
        CardEffect effect = new BoostTargetCreatureEffect(1, 1);
        card.target(1, 1).addEffect(EffectSlot.SPELL, effect);

        UUID a = UUID.randomUUID();
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, CONTROLLER, "test",
                List.of(), 0, a, null);

        assertThat(entry.targetsForEffect(effect)).containsExactly(a);
    }

    @Test
    @DisplayName("Aura entries keep the enchant target in targetId; the flat list starts at group 1")
    void auraEnchantTargetHeldSeparately() {
        Card card = new Card();
        card.setSubtypes(List.of(CardSubtype.AURA));
        CardEffect etbEffect = new BoostTargetCreatureEffect(1, 1);
        card.target(1, 1); // group 0: the enchant target
        card.target(1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, etbEffect);

        UUID enchanted = UUID.randomUUID();
        UUID etbTarget = UUID.randomUUID();
        StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, card, CONTROLLER, "test",
                List.of(), 0, enchanted, null, java.util.Map.of(), null, List.of(), List.of(etbTarget));

        assertThat(entry.targetsForGroup(0)).containsExactly(enchanted);
        assertThat(entry.targetsForGroup(1)).containsExactly(etbTarget);
        assertThat(entry.targetsForEffect(etbEffect)).containsExactly(etbTarget);
    }

    @Test
    @DisplayName("Aura entry with no ETB targets: a later-group effect gets nothing, not the enchant target")
    void auraWithoutEtbTargets() {
        Card card = new Card();
        card.setSubtypes(List.of(CardSubtype.AURA));
        CardEffect etbEffect = new BoostTargetCreatureEffect(1, 1);
        card.target(1, 1); // group 0: the enchant target
        card.target(0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, etbEffect);

        UUID enchanted = UUID.randomUUID();
        StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, card, CONTROLLER, "test",
                List.of(), 0, enchanted, null, java.util.Map.of(), null, List.of(), List.of());

        assertThat(entry.targetsForGroup(0)).containsExactly(enchanted);
        assertThat(entry.targetsForEffect(etbEffect)).isEmpty();
    }

    @Test
    @DisplayName("Wrapper effects register their inner effect under the same group")
    void wrapperInnerEffectSharesGroup() {
        Card card = new Card();
        CardEffect inner = new BoostTargetCreatureEffect(1, 0);
        CardEffect wrapper = new ConditionalEffect(new Kicked(), inner);
        card.target(1, 1);
        card.target(1, 1).addEffect(EffectSlot.SPELL, wrapper);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        StackEntry entry = spellEntry(card, List.of(a, b));

        assertThat(entry.targetsForEffect(wrapper)).containsExactly(b);
        assertThat(entry.targetsForEffect(inner)).containsExactly(b);
    }
}
