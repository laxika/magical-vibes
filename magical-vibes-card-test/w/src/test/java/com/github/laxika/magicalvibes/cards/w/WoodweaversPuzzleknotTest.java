package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodweaversPuzzleknot.class})
class WoodweaversPuzzleknotTest extends BaseCardTest {

    @Test
    void enteringBattlefieldGainsLifeAndEnergy() {
        harness.castFromHand(player1, new WoodweaversPuzzleknot(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void sacrificeAbilityGainsLifeAndEnergy() {
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new WoodweaversPuzzleknot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int puzzleknotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(puzzleknot);
        harness.activateAbility(player1, puzzleknotIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
    }

    @Test
    void tappedPuzzleknotIsSacrificedAsCostBeforeRewardsResolve() {
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new WoodweaversPuzzleknot());
        puzzleknot.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
        harness.assertLife(player1, 20);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void sacrificingBeforeEnterTriggerResolvesStillGrantsBothRewards() {
        harness.castFromHand(player1, new WoodweaversPuzzleknot(), "{2}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(6);
        harness.assertNotOnBattlefield(player1, "Woodweaver's Puzzleknot");
        harness.assertInGraveyard(player1, "Woodweaver's Puzzleknot");
        harness.assertLife(player2, 20);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
