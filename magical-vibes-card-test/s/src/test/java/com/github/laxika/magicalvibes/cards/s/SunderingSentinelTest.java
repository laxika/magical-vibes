package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunderingSentinel.class})
class SunderingSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB uses intensity, then intensifies, and returns at the next upkeep with haste")
    void etbAndDelayedReturnUsePersistentIntensity() {
        SunderingSentinel sentinel = new SunderingSentinel();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(sentinel));
        addSentinelMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getCardIntensity(sentinel.getId())).isEqualTo(3);

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Sundering Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sentinel);

        advanceToControllerUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent returned = findSentinel(player1);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.getCardIntensity(sentinel.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("A stolen Sentinel stays exiled during its owner's upkeep")
    void stolenSentinelDoesNotReturnAtOwnersUpkeep() {
        SunderingSentinel sentinel = new SunderingSentinel();
        sentinel.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, sentinel);

        advanceToEndStep();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(sentinel);

        advanceToControllerUpkeep(player2);

        harness.assertNotOnBattlefield(player2, "Sundering Sentinel");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(sentinel);
    }

    @Test
    @DisplayName("A stolen Sentinel returns to its owner at the previous controller's upkeep")
    void stolenSentinelReturnsAtPreviousControllersUpkeep() {
        SunderingSentinel sentinel = new SunderingSentinel();
        sentinel.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, sentinel);

        advanceToEndStep();
        advanceToControllerUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findSentinel(player2);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
        assertThat(gd.getCardIntensity(sentinel.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("The opponent's end step does not exile Sentinel")
    void doesNotExileAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new SunderingSentinel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sundering Sentinel");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("An illegal entry target prevents both life gain and intensifying")
    void illegalTargetPreventsEntireEntryAbility() {
        SunderingSentinel sentinel = new SunderingSentinel();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunderingSentinel());
        harness.setHand(player1, List.of(sentinel));
        addSentinelMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getCardIntensity(sentinel)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Sundering Sentinel");
    }
    private void addSentinelMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToControllerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findSentinel(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Sundering Sentinel"))
                .findFirst()
                .orElse(null);
    }
}
