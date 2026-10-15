package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperiousPerfect.class, LlanowarElves.class, GrizzlyBears.class, ElvishArchdruid.class,
        NamelessInversion.class})
class ImperiousPerfectTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherOwnElves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Imperious Perfect does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent perfect = findPermanent(player1, "Imperious Perfect");
        assertThat(gqs.getEffectivePower(gd, perfect)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perfect)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Elves")
    void doesNotBuffOpponentElves() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent opponentElf = findPermanent(player2, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Buffs another Elf lord (interaction with Elvish Archdruid)")
    void buffsOtherElfLord() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        // Archdruid is a 2/2 Elf, +1/+1 from Imperious Perfect
        Permanent archdruid = findPermanent(player1, "Elvish Archdruid");
        assertThat(gqs.getEffectivePower(gd, archdruid)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archdruid)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new ImperiousPerfect());
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
        addCreatureReady(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Created Elf Warrior token is itself buffed by the lord effect")
    void createdTokenIsBuffed() {
        addCreatureReady(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addCreatureReady(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ImperiousPerfect());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires tap and cannot be reused the same turn")
    void abilityRequiresTap() {
        addCreatureReady(player1, new ImperiousPerfect());
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

    @Test
    @DisplayName("The ability creates exactly one untapped green creature token for its controller")
    void tokenHasCorrectColorTypeAndController() {
        addCreatureReady(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elf Warrior")).isZero();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Two Perfects buff one another and their bonuses stack on other Elves")
    void multiplePerfectsStackTheirBonuses() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new LlanowarElves());

        for (Permanent perfect : findPermanents(player1, "Imperious Perfect")) {
            assertThat(gqs.getEffectivePower(gd, perfect)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, perfect)).isEqualTo(3);
        }
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green mana cannot pay the activation cost")
    void cannotActivateWithWrongColorMana() {
        Permanent perfect = addCreatureReady(player1, new ImperiousPerfect());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(perfect.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Token ability resolves after Perfect dies and its static bonus ends")
    void abilityResolvesAfterSourceDies() {
        Permanent perfect = addCreatureReady(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, perfect.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Imperious Perfect");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }
}
