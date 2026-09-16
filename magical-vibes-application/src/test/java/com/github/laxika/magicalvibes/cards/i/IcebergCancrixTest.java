package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;




@CardUsed({IcebergCancrix.class, SnowCoveredIsland.class, GrizzlyBears.class})
class IcebergCancrixTest extends BaseCardTest {

    @Test
    @DisplayName("May mill two cards when another snow permanent enters under your control")
    void mayMillForControlledSnowPermanent() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("May decline the snow-entry mill")
    void mayDeclineMill() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for nonsnow permanents or an opponent's snow permanent")
    void ignoresNonsnowAndOpponentSnowEntries() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.enterBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for Iceberg Cancrix entering itself")
    void ignoresItsOwnEntry() {
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new IcebergCancrix());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
    }
}

@CardUsed({IcebergCancrix.class, Forest.class, SnowCoveredIsland.class})
class Mh1IcebergCancrixTest extends BaseCardTest {

    @Test
    @DisplayName("A snow permanent entering under your control may mill a target player two cards")
    void ownSnowPermanentMayMillTargetPlayer() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player1, List.of(new SnowCoveredIsland()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the may ability does not mill")
    void decliningMayDoesNotMill() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player1, List.of(new SnowCoveredIsland()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nonsnow permanents and opposing snow permanents do not trigger the ability")
    void onlyOwnSnowPermanentsTrigger() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SnowCoveredIsland()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Iceberg Cancrix does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new IcebergCancrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
