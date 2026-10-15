package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarinthGreatwurm.class, Forest.class})
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

    @Test
    @DisplayName("Lands put onto the battlefield trigger for either player")
    void createsPowerstonesForLandsPutOntoBattlefield() {
        harness.addToBattlefield(player1, new SarinthGreatwurm());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Powerstone"))
                .hasSize(2)
                .allSatisfy(powerstone -> assertThat(powerstone.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("Each Greatwurm creates a Powerstone for its controller")
    void eachGreatwurmTriggersForTheSameLand() {
        harness.addToBattlefield(player1, new SarinthGreatwurm());
        harness.addToBattlefield(player2, new SarinthGreatwurm());

        playLand(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone"))
                .hasSize(1)
                .allSatisfy(powerstone -> assertThat(powerstone.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Powerstone"))
                .hasSize(1)
                .allSatisfy(powerstone -> assertThat(powerstone.isTapped()).isTrue());
    }

    @Test
    @DisplayName("A nonland entering does not create a Powerstone")
    void nonlandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarinthGreatwurm());

        harness.enterBattlefieldAndReturn(player2, new SarinthGreatwurm());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Forest()));
        harness.playLand(player, 0);
        harness.passBothPriorities();
    }
}
