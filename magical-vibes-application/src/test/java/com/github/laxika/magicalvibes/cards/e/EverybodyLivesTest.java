package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverybodyLives.class, GrizzlyBears.class, Shock.class})
class EverybodyLivesTest extends BaseCardTest {

    @Test
    @DisplayName("Protects all creatures and players and prevents every player's win or loss")
    void protectsEveryone() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castEverybodyLives();

        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isTrue();
        assertThat(gqs.canPlayerLoseLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseLife(gd, player2.getId())).isFalse();
        assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isFalse();
        assertThat(gqs.playerHasCantWinGameEffect(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Stops damage from reducing life during the turn")
    void damageDoesNotReduceLife() {
        harness.setLife(player2, 20);
        castEverybodyLives();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Player hexproof prevents an opponent from targeting that player")
    void playerHexproofPreventsOpponentTargeting() {
        castEverybodyLives();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("All protections expire during cleanup")
    void expiresAtCleanup() {
        harness.setLife(player1, 1);
        castEverybodyLives();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castEverybodyLives() {
        harness.setHand(player1, List.of(new EverybodyLives()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
