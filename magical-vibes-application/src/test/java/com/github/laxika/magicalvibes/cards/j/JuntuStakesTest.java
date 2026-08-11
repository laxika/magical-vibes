package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JuntuStakesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature with power 1 or less does not untap while Juntu Stakes is out")
    void power1CreatureStaysTapped() {
        addReady(player1, new JuntuStakes());
        Permanent creature = addReady(player1, new FugitiveWizard());
        creature.tap();

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature with power greater than 1 untaps normally")
    void power2CreatureUntaps() {
        addReady(player1, new JuntuStakes());
        Permanent creature = addReady(player1, new GrizzlyBears());
        creature.tap();

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' creatures during their untap step")
    void affectsOpponentCreatures() {
        addReady(player1, new JuntuStakes());
        Permanent opponentCreature = addReady(player2, new FugitiveWizard());
        opponentCreature.tap();

        advanceToNextTurn(player1);

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Juntu Stakes leaves, low-power creatures untap again")
    void untapsAfterStakesLeaves() {
        Permanent stakes = addReady(player1, new JuntuStakes());
        Permanent creature = addReady(player1, new FugitiveWizard());
        creature.tap();

        gd.playerBattlefields.get(player1.getId()).remove(stakes);

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
