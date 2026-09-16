package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfGrace;
import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestorsProphet.class, DiscipleOfGrace.class, FesteringGoblin.class})
class AncestorsProphetTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five untapped Clerics gains 10 life")
    void tapsFiveClericsAndGainsTenLife() {
        harness.setLife(player1, 10);
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new AncestorsProphet());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new DiscipleOfGrace());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(Permanent::isTapped);
        assertThat(prophet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires five untapped Clerics")
    void cannotActivateWithoutFiveUntappedClerics() {
        harness.addToBattlefield(player1, new AncestorsProphet());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DiscipleOfGrace());
        }
        harness.addToBattlefield(player1, new FesteringGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Cleric cannot be tapped again to pay the cost")
    void tappedClericDoesNotCount() {
        harness.addToBattlefield(player1, new AncestorsProphet());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DiscipleOfGrace());
        }
        Permanent tappedCleric = harness.addToBattlefieldAndReturn(player1, new DiscipleOfGrace());
        tappedCleric.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Cleric cannot be tapped to pay the cost")
    void opponentClericsDoNotCount() {
        harness.addToBattlefield(player1, new AncestorsProphet());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DiscipleOfGrace());
        }
        harness.addToBattlefield(player2, new DiscipleOfGrace());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(permanent -> !permanent.isTapped());
    }
}
