package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuminatePrimordial.class, GrizzlyBears.class, ArmoredTransport.class})
class LuminatePrimordialTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature and its controller gains its effective power")
    void exilesTargetCreatureAndGainsLifeEqualToPower() {
        harness.setLife(player2, 10);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setPowerModifier(3);

        castLuminatePrimordial(List.of(bear.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        castLuminatePrimordial(List.of());

        harness.assertOnBattlefield(player1, "Luminate Primordial");
    }

    @Test
    @DisplayName("Cannot choose two creatures controlled by the same opponent")
    void cannotChooseTwoCreaturesControlledBySameOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LuminatePrimordial()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Exiles a creature for each opponent and gives each opponent its creature's power")
    void exilesOneCreatureForEachOpponent() {
        Player player3 = addThirdPlayer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent second = harness.addToBattlefieldAndReturn(player3, new ArmoredTransport());
        second.setPowerModifier(3);

        castWithThreePlayers(player3, List.of(first.getId(), second.getId()));
        passUntilStackSize(player3, 0);

        harness.assertNotOnBattlefield(player2, "Armored Transport");
        harness.assertNotOnBattlefield(player3, "Armored Transport");
        harness.assertLife(player2, 22);
        harness.assertLife(player3, 25);
    }

    @Test
    @DisplayName("An opponent still gains life for an illegal target when another target remains legal")
    void gainsLifeForCreatureThatLeftBeforeResolution() {
        Player player3 = addThirdPlayer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        first.setPowerModifier(3);
        Permanent second = harness.addToBattlefieldAndReturn(player3, new ArmoredTransport());

        castWithThreePlayers(player3, List.of(first.getId(), second.getId()));
        assertThat(harness.getPermanentRemovalService().removePermanentToExile(gd, first)).isTrue();
        passUntilStackSize(player3, 0);

        harness.assertNotOnBattlefield(player3, "Armored Transport");
        harness.assertLife(player2, 25);
        harness.assertLife(player3, 22);
    }

    @Test
    @DisplayName("Can decline to exile an opponent's available creature")
    void canDeclineAvailableTarget() {
        harness.addToBattlefield(player2, new ArmoredTransport());

        castLuminatePrimordial(List.of());

        harness.assertOnBattlefield(player2, "Armored Transport");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the ability's controller")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new LuminatePrimordial()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling a negative-power creature does not cause life loss")
    void negativePowerDoesNotCauseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        target.setPowerModifier(-3);

        castLuminatePrimordial(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Armored Transport");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is gained when the only target leaves before resolution")
    void noLifeGainedWhenAllTargetsAreIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new LuminatePrimordial()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Luminate Primordial");
        assertThat(harness.getPermanentRemovalService().removePermanentToExile(gd, target)).isTrue();

        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Luminate Primordial attacks without tapping")
    void attacksWithoutTapping() {
        Permanent primordial = addCreatureReady(player1, new LuminatePrimordial());

        declareAttackers(List.of(0));

        assertThat(primordial.isTapped()).isFalse();
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player3 = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player3;
    }

    private void castWithThreePlayers(Player player3, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new LuminatePrimordial()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, targetIds);
        for (int i = 0; i < 20 && gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getName().equals("Luminate Primordial")); i++) {
            passNextPriority(player3);
        }
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Luminate Primordial");
    }

    private void passUntilStackSize(Player player3, int expectedSize) {
        for (int i = 0; i < 20 && gd.stack.size() != expectedSize; i++) {
            passNextPriority(player3);
        }
        assertThat(gd.stack).hasSize(expectedSize);
    }

    private void passNextPriority(Player player3) {
        UUID priorityHolder = gqs.getPriorityPlayerId(gd);
        Player player = List.of(player1, player2, player3).stream()
                .filter(candidate -> candidate.getId().equals(priorityHolder))
                .findFirst().orElseThrow();
        gs.passPriority(gd, player);
    }

    private void castLuminatePrimordial(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new LuminatePrimordial()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }
}
