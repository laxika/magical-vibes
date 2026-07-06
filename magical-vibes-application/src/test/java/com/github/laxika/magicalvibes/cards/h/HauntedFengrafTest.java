package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HauntedFengrafTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HauntedFengraf());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Sacrifice ability: activation =====

    @Test
    @DisplayName("Activating sacrifice ability puts the return on the stack")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Haunted Fengraf");
    }

    @Test
    @DisplayName("Haunted Fengraf is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Haunted Fengraf"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Haunted Fengraf"));
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Sacrifice ability: resolution =====

    @Test
    @DisplayName("Resolving returns a creature card from graveyard to hand")
    void resolvingReturnsCreature() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Returns exactly one creature at random when several are in the graveyard")
    void returnsOneRandomCreatureFromMultiple() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        long handBears = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(handBears).isEqualTo(1);

        long graveyardBears = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(graveyardBears).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-creature cards in the graveyard are ignored")
    void ignoresNonCreatures() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // The creature is returned, the instant stays in the graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Resolves without returning anything when no creatures are in the graveyard")
    void doesNothingWithNoCreatures() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate sacrifice ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
