package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiskyMove.class, ElvishWarrior.class})
class RiskyMoveTest extends BaseCardTest {

    @Test
    @DisplayName("The active player gains Risky Move and resolves its coin flip")
    void activePlayerGainsControlAndFlips() {
        Permanent riskyMove = harness.addToBattlefieldAndReturn(player1, new RiskyMove());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Risky Move");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(riskyMove);
        assertThat(gd.playerBattlefields.get(won ? player2.getId() : player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(won ? player1.getId() : player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Risky Move does not flip when its new controller controls no creatures")
    void noCreatureMeansNoCoinFlip() {
        Permanent riskyMove = harness.addToBattlefieldAndReturn(player1, new RiskyMove());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(riskyMove);
        assertThat(gameLogContains("coin flip for Risky Move")).isFalse();
    }

    @Test
    @DisplayName("Risky Move asks its new controller to choose among multiple creatures")
    void choosesCreatureBeforeFlipping() {
        harness.addToBattlefield(player1, new RiskyMove());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());

        harness.handlePermanentChosen(player2, firstCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("coin flip for Risky Move")).isTrue();
    }

    @Test
    @DisplayName("Risky Move does not trigger its rider during its controller's own upkeep")
    void ownUpkeepDoesNotTriggerRider() {
        Permanent riskyMove = harness.addToBattlefieldAndReturn(player1, new RiskyMove());
        harness.addToBattlefield(player1, new ElvishWarrior());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riskyMove);
        assertThat(gameLogContains("coin flip for Risky Move")).isFalse();
    }

    @Test
    @DisplayName("Risky Move triggers again when the next player's upkeep takes it back")
    void triggersAgainWhenControlReturnsOnNextUpkeep() {
        Permanent riskyMove = harness.addToBattlefieldAndReturn(player1, new RiskyMove());
        harness.addToBattlefield(player1, new ElvishWarrior());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(riskyMove);
        assertThat(gameLogContains("coin flip for Risky Move")).isFalse();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riskyMove);
        assertThat(gameLogContains("coin flip for Risky Move")).isTrue();
    }
}
