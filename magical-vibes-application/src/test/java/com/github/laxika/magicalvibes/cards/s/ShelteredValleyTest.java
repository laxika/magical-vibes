package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShelteredValleyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices every other Sheltered Valley you control")
    void entrySacrificesOtherCopies() {
        harness.addToBattlefield(player1, new ShelteredValley());
        harness.addToBattlefield(player1, new ShelteredValley());
        harness.setHand(player1, List.of(new ShelteredValley()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Sheltered Valley");
    }

    @Test
    @DisplayName("An opponent's Sheltered Valley is not sacrificed")
    void opponentCopySurvives() {
        harness.addToBattlefield(player2, new ShelteredValley());
        harness.setHand(player1, List.of(new ShelteredValley()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Sheltered Valley");
        harness.assertOnBattlefield(player2, "Sheltered Valley");
    }

    @Test
    @DisplayName("Upkeep gains 1 life while you control three or fewer lands")
    void upkeepGainsLifeWithFewLands() {
        harness.addToBattlefield(player1, new ShelteredValley());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep gains no life with four or more lands")
    void upkeepGainsNothingWithManyLands() {
        harness.addToBattlefield(player1, new ShelteredValley());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Mana ability adds {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new ShelteredValley());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
