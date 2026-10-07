package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RhoxFaithmender;
import com.github.laxika.magicalvibes.cards.s.SilvercoatLion;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TouchOfTheEternal.class, SilvercoatLion.class, Plains.class, Naturalize.class, RhoxFaithmender.class})
class TouchOfTheEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger sets life to the number of permanents you control")
    void upkeepSetsLifeToPermanentCount() {
        addTouch(player1);
        addLion(player1);
        addLion(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        runUpkeep(player1);

        harness.assertLife(player1, 3); // the enchantment itself plus two Lions
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Permanents controlled by the opponent are not counted")
    void opponentPermanentsAreNotCounted() {
        addTouch(player1);
        addLion(player2);
        addLion(player2);
        addLion(player2);
        harness.setLife(player1, 20);

        runUpkeep(player1);

        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Life can go up as well as down")
    void lifeCanIncrease() {
        addTouch(player1);
        for (int i = 0; i < 9; i++) {
            addLion(player1);
        }
        harness.setLife(player1, 4);

        runUpkeep(player1);

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("The trigger does not fire during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addTouch(player1);
        addLion(player1);
        harness.setLife(player1, 20);

        runUpkeep(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Lands count toward the life total")
    void landsAreCounted() {
        addTouch(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 20);

        runUpkeep(player1);

        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("A pending trigger resolves after its source is destroyed and counts the remaining permanents")
    void countsRemainingPermanentsAfterSourceIsDestroyed() {
        Permanent touch = addTouch(player1);
        addLion(player1);
        harness.setLife(player1, 20);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        destroyTouch(touch);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("A pending trigger sets life to zero when no permanents remain")
    void noRemainingPermanentsCausesLifeToBecomeZero() {
        Permanent touch = addTouch(player1);
        harness.setLife(player1, 20);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        destroyTouch(touch);
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Setting life to a higher total gains life and applies life-gain replacements")
    void lifeGainIsDoubledByRhoxFaithmender() {
        addTouch(player1);
        harness.addToBattlefield(player1, new RhoxFaithmender());
        addLion(player1);
        harness.setLife(player1, 1);

        runUpkeep(player1);

        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Setting life to the current total does not gain life")
    void equalLifeTotalDoesNotGainLife() {
        addTouch(player1);
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 2);

        runUpkeep(player1);

        harness.assertLife(player1, 2);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    private void destroyTouch(Permanent touch) {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, touch.getId());
    }

    private Permanent addTouch(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new TouchOfTheEternal());
    }

    private void addLion(Player owner) {
        harness.addToBattlefield(owner, new SilvercoatLion());
    }

    private void runUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // resolve it
    }
}
