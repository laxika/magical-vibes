package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.d.DesertOfTheFervent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazezonShaperOfSand.class, Desert.class, DesertOfTheFervent.class, Forest.class})
class HazezonShaperOfSandTest extends BaseCardTest {

    @Test
    @DisplayName("The Desert card can also be played from the graveyard")
    void canPlayDesertCardFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new Desert()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Desert");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can play a Desert from the controller's graveyard")
    void canPlayDesertFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Desert of the Fervent");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot play a non-Desert land from the controller's graveyard")
    void cannotPlayNonDesertFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("A Desert entering under your control creates two multicolored Sand Warriors")
    void desertLandfallCreatesSandWarriors() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new DesertOfTheFervent()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Sand Warrior");
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        }
    }

    @Test
    @DisplayName("A non-Desert land does not create Sand Warriors")
    void nonDesertLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
    }

    @Test
    void createdTokensHaveBothSandAndWarriorCreatureTypes() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new DesertOfTheFervent()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getSubtypes())
                        .containsExactlyInAnyOrder(CardSubtype.SAND, CardSubtype.WARRIOR));
    }

    @Test
    void playingDesertFromGraveyardAlsoTriggersTokenCreation() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(2);
        assertThat(findPermanent(player1, "Desert of the Fervent").isTapped()).isTrue();
    }

    @Test
    void opponentDesertDoesNotTrigger() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player2, List.of(new DesertOfTheFervent()));
        prepareMainPhase(player2);

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Sand Warrior")).isEmpty();
    }

    @Test
    void puttingDesertOntoBattlefieldWithoutPlayingItTriggers() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());

        harness.enterBattlefieldAndReturn(player1, new DesertOfTheFervent());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(2);
    }

    @Test
    void graveyardPermissionDoesNotGrantAnAdditionalLandPlay() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        prepareMainPhase(player1);
        harness.playLand(player1, 0);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Desert of the Fervent");
        harness.assertNotOnBattlefield(player1, "Desert of the Fervent");
    }

    @Test
    void cannotPlayDesertFromGraveyardOutsideMainPhase() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Desert of the Fervent");
    }

    @Test
    void cannotPlayDesertFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        DesertOfTheFervent desert = new DesertOfTheFervent();
        harness.setGraveyard(player2, List.of(desert));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, desert.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Desert of the Fervent");
    }

    @Test
    void cannotPlayGraveyardDesertWhileLandfallTriggerIsOnStack() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        prepareMainPhase(player1);
        harness.enterBattlefieldAndReturn(player1, new DesertOfTheFervent());
        assertThat(gd.stack).isNotEmpty();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Desert of the Fervent");
        resolveAllTriggers();
    }

    @Test
    void cannotPlayGraveyardDesertWithoutHazezonOnBattlefield() {
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Desert of the Fervent");
    }

    @Test
    void desertwalkPreventsBlockingWhenDefenderControlsDesert() {
        addCreatureReady(player1, new HazezonShaperOfSand());
        harness.addToBattlefield(player2, new HazezonShaperOfSand());
        harness.addToBattlefield(player2, new DesertOfTheFervent());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("desertwalk");
    }

    @Test
    void desertwalkAllowsBlockingWhenOnlyAttackerControlsDesert() {
        addCreatureReady(player1, new HazezonShaperOfSand());
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player2, new HazezonShaperOfSand());
        harness.addToBattlefield(player2, new Forest());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Hazezon, Shaper of Sand");
        harness.assertInGraveyard(player2, "Hazezon, Shaper of Sand");
        harness.assertLife(player2, 20);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
