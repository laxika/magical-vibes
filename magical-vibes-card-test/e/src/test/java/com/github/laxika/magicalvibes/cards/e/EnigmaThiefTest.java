package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnigmaThief.class, GrizzlyBears.class, Plains.class})
class EnigmaThiefTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one nonland permanent per opponent")
    void returnsOneNonlandPermanentPerOpponent() {
        Player third = addOpponent();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new GrizzlyBears());

        castAndResolve(List.of(first.getId(), second.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(third, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(third, "Grizzly Bears");
    }

    @Test
    void canChooseNoTargets() {
        castAndResolve(List.of());

        harness.assertOnBattlefield(player1, "Enigma Thief");
    }

    @Test
    void cannotTargetLandOrOwnPermanent() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTwoPermanentsControlledBySameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private void castAndResolve(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EnigmaThief()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(opponent.getUsername());
        gd.playerIdToName.put(opponent.getId(), opponent.getUsername());
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
