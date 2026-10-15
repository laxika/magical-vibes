package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecondWind.class, GrizzlyBears.class, Island.class})
class SecondWindTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Second Wind attaches it to a target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Second Wind")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Second Wind can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Second Wind");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The tap ability taps both the enchanted creature and Second Wind")
    void tapAbilityTapsEnchantedCreatureAndAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability untaps the enchanted creature and taps Second Wind")
    void untapAbilityUntapsEnchantedCreatureAndTapsAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second Wind can target only a creature")
    void cannotEnchantIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("The Aura controller can tap or untap an opponent's enchanted creature")
    void abilitiesAffectOpponentsCreature(int abilityIndex) {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        if (abilityIndex == 1) {
            creature.tap();
        }
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(aura.isTapped()).isTrue();
        assertThat(creature.isTapped()).isEqualTo(abilityIndex == 1);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isEqualTo(abilityIndex == 0);
        assertThat(aura.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated while Second Wind is tapped")
    void tappedAuraCannotActivateEitherAbility(int abilityIndex) {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);
        aura.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either ability is legal even if the enchanted creature's state will not change")
    void abilityCanResolveWithoutChangingCreatureState(int abilityIndex) {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        if (abilityIndex == 0) {
            creature.tap();
        }
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 1, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isEqualTo(abilityIndex == 0);
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second Wind can activate immediately after resolving on a new creature")
    void canActivateImmediatelyAfterResolving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Second Wind");

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(aura.isTapped()).isTrue();
    }

    private Permanent addAttachedAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SecondWind());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
