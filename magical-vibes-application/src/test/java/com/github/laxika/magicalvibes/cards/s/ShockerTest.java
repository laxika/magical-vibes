package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShockerTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player discards their hand and draws that many cards")
    void damagedPlayerWheelsTheirHand() {
        addAttackingShocker(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty-handed damaged player draws nothing")
    void emptyHandDrawsNothing() {
        addAttackingShocker(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The attacking player's own hand is untouched")
    void controllerHandUntouched() {
        addAttackingShocker(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addAttackingShocker(Player player) {
        Permanent shocker = addCreatureReady(player, new Shocker());
        shocker.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
