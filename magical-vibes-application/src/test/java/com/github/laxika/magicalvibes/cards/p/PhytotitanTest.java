package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhytotitanTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns tapped at the beginning of its owner's next upkeep")
    void diesThenReturnsTappedAtOwnersNextUpkeep() {
        killPhytotitan(player1);

        harness.assertInGraveyard(player1, "Phytotitan");
        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // the end step must not return it

        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNull();

        runUpkeepOf(player1);

        Permanent returned = findPermanentOrNull(player1, "Phytotitan");
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Phytotitan");
    }

    @Test
    @DisplayName("Does not return at an opponent's upkeep")
    void doesNotReturnAtOpponentUpkeep() {
        killPhytotitan(player1);

        runUpkeepOf(player2);

        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNull();
        harness.assertInGraveyard(player1, "Phytotitan");

        runUpkeepOf(player1);

        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNotNull();
    }

    @Test
    @DisplayName("Returns only once per death")
    void returnsOnlyOncePerDeath() {
        killPhytotitan(player1);

        runUpkeepOf(player1);
        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNotNull();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Phytotitan"));

        runUpkeepOf(player1);

        assertThat(findPermanentOrNull(player1, "Phytotitan")).isNull();
    }

    private void runUpkeepOf(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance into the upkeep, firing its delayed triggers
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void killPhytotitan(Player player) {
        Permanent titan = harness.addToBattlefieldAndReturn(player, new Phytotitan());
        titan.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve the dies trigger
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
