package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeafeningSilence.class, GrizzlyBears.class, Shock.class, Cancel.class})
class DeafeningSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("The first noncreature spell each turn is castable")
    void allowsFirstNoncreatureSpell() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("A player cannot cast a second noncreature spell in the same turn")
    void preventsSecondNoncreatureSpell() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature spells do not count toward Deafening Silence's limit")
    void doesNotLimitCreatureSpells() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Deafening Silence limits noncreature spells for both players")
    void affectsBothPlayers() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Casting Deafening Silence uses the caster's noncreature spell allowance")
    void countsItsOwnCasting() {
        harness.setHand(player1, List.of(new DeafeningSilence(), new DeafeningSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deafening Silence");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells cast before Deafening Silence enters still count")
    void countsEarlierNoncreatureSpell() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player2, new DeafeningSilence());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple creatures remain castable after the noncreature allowance is used")
    void allowsMultipleCreaturesAfterNoncreatureSpell() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Each player has a separate allowance during the same turn")
    void allowsBothPlayersOneNoncreatureSpellInSameTurn() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("The allowance resets on the opponent's turn")
    void resetsAllowanceOnNextTurn() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A countered noncreature spell still uses the allowance")
    void countsCounteredNoncreatureSpell() {
        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
