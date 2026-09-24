package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OftNabbedGoat.class, Forest.class, Shock.class})
class OftNabbedGoatTest extends BaseCardTest {

    @Test
    @DisplayName("Only an opponent can activate it, drawing a card and gaining control")
    void opponentActivatesAndGainsControl() {
        Permanent goat = addCreatureReady(player1, new OftNabbedGoat());
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(goat);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat);
        assertThat(goat.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isOne();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Its controller cannot activate the opponent-only ability")
    void controllerCannotActivateAbility() {
        addCreatureReady(player1, new OftNabbedGoat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents may activate this ability");
    }

    @Test
    @DisplayName("On death, its owner draws and every other player loses life per counter")
    void ownerDrawsAndOtherPlayersLoseLifeOnDeath() {
        Permanent goat = addCreatureReady(player1, new OftNabbedGoat());
        goat.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, goat.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Oft-Nabbed Goat");
    }
}
