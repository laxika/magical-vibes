package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BottleGnomes.class)
class BottleGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Bottle Gnomes gains 3 life for its controller")
    void sacrificeGainsThreeLife() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(gnomes.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(gnomes.getCard().getId()));
    }

    @Test
    @DisplayName("Sacrifice is paid before Bottle Gnomes's ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 10);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(gnomes.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(gnomes.getCard().getId()));

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Bottle Gnomes's sacrifice ability can be activated while summoning sick")
    void sacrificeAbilityCanBeActivatedWhileSummoningSick() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(gnomes.getCard().getId()));
    }

    @Test
    @DisplayName("Tapped Bottle Gnomes can still be sacrificed for life")
    void sacrificeAbilityCanBeActivatedWhileTapped() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        gnomes.tap();
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Bottle Gnomes can gain life for its controller during the opponent's turn")
    void sacrificeAbilityCanBeActivatedDuringOpponentsTurn() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 10);
        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");

        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 20);
    }
}
