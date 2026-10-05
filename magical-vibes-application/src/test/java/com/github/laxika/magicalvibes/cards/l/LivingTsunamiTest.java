package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingTsunami.class, Island.class, Plains.class})
class LivingTsunamiTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        addTsunami();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Living Tsunami");
        harness.assertInGraveyard(player1, "Living Tsunami");
    }

    @Test
    @DisplayName("Returning a land keeps Living Tsunami")
    void returningLandKeepsTsunami() {
        Permanent tsunami = addTsunami();
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("A tapped land can be returned")
    void returningTappedLandKeepsTsunami() {
        Permanent tsunami = addTsunami();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Declining to return a land sacrifices Living Tsunami")
    void decliningSacrificesTsunami() {
        addTsunami();
        harness.addToBattlefield(player1, new Plains());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Living Tsunami");
        harness.assertInGraveyard(player1, "Living Tsunami");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent tsunami = addTsunami();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
    }

    @Test
    @DisplayName("An opponent's land cannot pay the upkeep cost")
    void opponentLandCannotPayCost() {
        addTsunami();
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Living Tsunami");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Controller chooses exactly one of multiple lands to return")
    void choosesOneLandToReturn() {
        Permanent tsunami = addTsunami();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(island.getId(), plains.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami, island).doesNotContain(plains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingIsland);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("A land controlled by the payer returns to its owner's hand")
    void returnsBorrowedLandToOwner() {
        addTsunami();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.stolenCreatures.put(island.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Living Tsunami");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getName().equals("Island"));
    }

    private Permanent addTsunami() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new LivingTsunami());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
