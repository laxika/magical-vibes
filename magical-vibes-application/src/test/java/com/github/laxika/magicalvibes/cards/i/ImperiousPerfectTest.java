package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImperiousPerfectTest extends BaseCardTest {

    // ===== Static effect: buffs other Elves you control =====

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherOwnElves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent elf = elf(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Imperious Perfect does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent perfect = elf(player1, "Imperious Perfect");
        assertThat(gqs.getEffectivePower(gd, perfect)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perfect)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent bears = elf(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Elves")
    void doesNotBuffOpponentElves() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent opponentElf = elf(player2, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Buffs another Elf lord (interaction with Elvish Archdruid)")
    void buffsOtherElfLord() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        // Archdruid is a 2/2 Elf, +1/+1 from Imperious Perfect
        Permanent archdruid = elf(player1, "Elvish Archdruid");
        assertThat(gqs.getEffectivePower(gd, archdruid)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archdruid)).isEqualTo(3);
    }

    // ===== Token creation via {G}, {T} activated ability =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addPerfectReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Imperious Perfect");
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 green Elf Warrior token")
    void resolvingAbilityCreatesToken() {
        addPerfectReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Created Elf Warrior token is itself buffed by the lord effect")
    void createdTokenIsBuffed() {
        addPerfectReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addPerfectReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addPerfectReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires tap and cannot be reused the same turn")
    void abilityRequiresTap() {
        addPerfectReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Now tapped: activating again is illegal
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the token ability with summoning sickness")
    void abilityBlockedBySummoningSickness() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent elf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    private Permanent addPerfectReady(Player player) {
        Permanent perm = new Permanent(new ImperiousPerfect());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
