package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoodweaversPuzzleknotTest extends BaseCardTest {

    @Test
    void enteringBattlefieldGainsLifeAndEnergy() {
        harness.setHand(player1, List.of(new WoodweaversPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
}
