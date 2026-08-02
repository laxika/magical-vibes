package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiSavantTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller cannot pay {3}")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, elves.getId());

        harness.assertInHand(player1, "Island");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell resolves when its controller pays {3}")
    void spellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Llanowar Elves");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Spell is countered when its controller declines to pay")
    void spellCounteredWhenControllerDeclines() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, elves.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Savant"), 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
