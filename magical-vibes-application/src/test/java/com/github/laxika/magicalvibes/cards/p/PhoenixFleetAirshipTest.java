package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhoenixFleetAirship.class, BottleGnomes.class})
class PhoenixFleetAirshipTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a token when no permanent was sacrificed")
    void doesNotCreateTokenWithoutSacrifice() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());

        advanceToEndStep();

        assertThat(findAirships()).hasSize(1);
    }

    @Test
    @DisplayName("Creates a token copy at your end step after you sacrifice a permanent")
    void createsTokenCopyAfterSacrifice() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int gnomesIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gnomes);
        harness.activateAbility(player1, gnomesIndex, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findAirships()).hasSize(2);
        assertThat(findAirships()).anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Becomes an artifact creature with eight permanents of its name")
    void becomesCreatureAtEightNamedPermanents() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new PhoenixFleetAirship());
        }

        Permanent airship = harness.addToBattlefieldAndReturn(player1, new PhoenixFleetAirship());

        assertThat(gqs.isCreature(gd, airship)).isTrue();
    }

    @Test
    @DisplayName("Opponent's named permanents do not count toward the creature threshold")
    void opponentNamedPermanentsDoNotCount() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new PhoenixFleetAirship());
        }
        Permanent airship = harness.addToBattlefieldAndReturn(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player2, new PhoenixFleetAirship());

        assertThat(gqs.isCreature(gd, airship)).isFalse();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> findAirships() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Phoenix Fleet Airship"))
                .toList();
    }
}
