package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeedwayFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("When Speedway Fanatic crews a Vehicle, it gains haste until end of turn")
    void vehicleGainsHasteWhenFanaticCrewsIt() {
        Permanent fanatic = addReady(player1, new SpeedwayFanatic());
        Permanent dreadnought = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, dreadnought), null, null);

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isTrue();

        harness.passBothPriorities();

        assertThat(fanatic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Speedway Fanatic does not trigger when another creature crews the Vehicle")
    void vehicleDoesNotGainHasteWhenAnotherCreatureCrewsIt() {
        addReady(player1, new SpeedwayFanatic());
        Permanent dreadnought = addReady(player1, new DuskLegionDreadnought());
        Permanent angel = addReady(player1, new SerraAngel());

        harness.activateAbility(player1, indexOf(player1, dreadnought), null, null);
        harness.handlePermanentChosen(player1, angel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The granted haste expires at end of turn")
    void grantedHasteExpiresAtEndOfTurn() {
        addReady(player1, new SpeedwayFanatic());
        Permanent dreadnought = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, dreadnought), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
