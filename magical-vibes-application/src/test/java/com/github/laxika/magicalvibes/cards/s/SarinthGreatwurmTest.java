package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SarinthGreatwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped Powerstone when a land you control enters")
    void createsPowerstoneForControllerLand() {
        harness.addToBattlefield(player1, new SarinthGreatwurm());
        playLand(player1);

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a tapped Powerstone when an opponent's land enters")
    void createsPowerstoneForOpponentLand() {
        harness.addToBattlefield(player1, new SarinthGreatwurm());
        playLand(player2);

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Forest()));
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }
}
