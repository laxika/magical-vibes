package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HesitationTest extends BaseCardTest {

    @Test
    @DisplayName("When any player casts a spell, Hesitation sacrifices itself and counters that spell")
    void sacrificesAndCountersAnyPlayersSpell() {
        harness.addToBattlefield(player1, new Hesitation());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hesitation");
        harness.assertInGraveyard(player1, "Hesitation");
        harness.assertInGraveyard(player2, "Opt");
        harness.assertNotOnBattlefield(player2, "Opt");
    }

    @Test
    @DisplayName("Playing a land does not trigger Hesitation")
    void playingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Hesitation());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hesitation");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
