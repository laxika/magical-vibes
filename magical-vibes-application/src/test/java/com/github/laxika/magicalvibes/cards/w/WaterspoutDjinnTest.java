package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterspoutDjinn.class, Island.class, Plains.class})
class WaterspoutDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Island")
    void autoSacrificesWithoutUntappedIsland() {
        addCreatureReady(player1, new WaterspoutDjinn());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        advanceToUpkeep(player1);
        island.tap();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Waterspout Djinn");
        harness.assertInGraveyard(player1, "Waterspout Djinn");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Accepting with one untapped Island returns it and keeps the Djinn")
    void acceptReturnsIslandAndKeepsDjinn() {
        Permanent djinn = addCreatureReady(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Island"))).isTrue();
    }

    @Test
    @DisplayName("Accepting with two untapped Islands lets controller choose which to return")
    void acceptWithTwoIslandsChoosesOne() {
        Permanent djinn = addCreatureReady(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID islandId = findPermanent(player1, "Island").getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(islandId));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Island")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining sacrifices the Djinn and keeps the Island")
    void declineSacrificesDjinn() {
        addCreatureReady(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Waterspout Djinn");
        harness.assertInGraveyard(player1, "Waterspout Djinn");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Does not use an untapped Island controlled by an opponent")
    void doesNotUseOpponentsIsland() {
        addCreatureReady(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Waterspout Djinn");
        harness.assertInGraveyard(player1, "Waterspout Djinn");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent djinn = addCreatureReady(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
    }
}
