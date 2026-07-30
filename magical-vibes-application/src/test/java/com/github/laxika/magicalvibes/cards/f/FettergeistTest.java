package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FettergeistTest extends BaseCardTest {

    private boolean controlsFettergeist(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Fettergeist"));
    }

    @Test
    @DisplayName("With no other creatures the cost is {0} and Fettergeist survives")
    void noOtherCreaturesCostsNothing() {
        harness.addToBattlefield(player1, new Fettergeist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsFettergeist(player1)).isTrue();
    }

    @Test
    @DisplayName("Paying {1} for each other creature you control keeps Fettergeist")
    void payingPerOtherCreatureKeepsIt() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsFettergeist(player1)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Fettergeist")
    void notEnoughManaSacrifices() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsFettergeist(player1)).isFalse();
    }

    @Test
    @DisplayName("Declining the payment sacrifices Fettergeist")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsFettergeist(player1)).isFalse();
    }

    @Test
    @DisplayName("Creatures controlled by the opponent do not raise the cost")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsFettergeist(player1)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(controlsFettergeist(player1)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
