package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenOfFire.class, Island.class, Mountain.class, Plains.class, GrizzlyBears.class,
        SavannahLions.class})
class OmenOfFireTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new OmenOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns every Island to its owner's hand, regardless of controller")
    void returnsAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        cast();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Returns a stolen Island to its owner's hand")
    void returnsStolenIslandToOwner() {
        harness.addToBattlefield(player1, new Island());
        Permanent stolenIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        gd.stolenCreatures.put(stolenIsland.getId(), player1.getId());
        harness.setHand(player2, List.of());

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player with no white permanents sacrifices nothing, even with Plains")
    void noWhitePermanentsMeansNoSacrifice() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(countPermanents(player2, "Plains")).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("One white creature and one Plains: both are eligible, exactly one is sacrificed")
    void sacrificesOnePerWhitePermanent() {
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new Plains());

        cast();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).hasSize(2);

        Permanent plains = findPermanents(player2, "Plains").getFirst();
        harness.handleMultiplePermanentsChosen(player2, List.of(plains.getId()));

        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Two white creatures and no Plains: both are sacrificed with no choice")
    void sacrificesAllWhenEligibleCountEqualsRequiredCount() {
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each player's sacrifice count is their own white permanent count")
    void countIsPerPlayer() {
        // player1 controls one white permanent, player2 controls two.
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new Plains());

        cast();

        GameData gd = harness.getGameData();

        // APNAP: the active player chooses first — 1 of 3 eligible permanents.
        PendingInteraction.MultiPermanentChoice first =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(first.playerId()).isEqualTo(player1.getId());
        assertThat(first.maxCount()).isEqualTo(1);
        UUID p1Plains = findPermanents(player1, "Plains").getFirst().getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Plains));

        // Nothing is sacrificed until every player has chosen (CR 101.4).
        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);

        PendingInteraction.MultiPermanentChoice second =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(second.playerId()).isEqualTo(player2.getId());
        assertThat(second.maxCount()).isEqualTo(2);
        List<UUID> p2Chosen = List.of(
                findPermanents(player2, "Plains").getFirst().getId(),
                findPermanents(player2, "Savannah Lions").getFirst().getId());
        harness.handleMultiplePermanentsChosen(player2, p2Chosen);

        assertThat(countPermanents(player1, "Plains")).isEqualTo(1);
        assertThat(countPermanents(player1, "Savannah Lions")).isEqualTo(1);
        assertThat(countPermanents(player2, "Plains")).isZero();
        assertThat(countPermanents(player2, "Savannah Lions")).isEqualTo(1);
    }
}
