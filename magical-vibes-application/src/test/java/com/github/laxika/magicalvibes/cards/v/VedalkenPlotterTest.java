package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenPlotter.class, Forest.class, Island.class})
class VedalkenPlotterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exchanges control of the two target lands")
    void exchangesControlOfLands() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("ETB fizzles when a target land leaves before resolution")
    void fizzlesWhenTargetGone() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target your own land as the opponent's land")
    void cannotTargetOwnLandAsOpponentTarget() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(own.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land an opponent controls");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
