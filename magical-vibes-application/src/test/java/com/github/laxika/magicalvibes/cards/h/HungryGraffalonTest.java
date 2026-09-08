package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HungryGraffalonTest extends BaseCardTest {

    private Permanent addGraffalon(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HungryGraffalon());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Nested
    @DisplayName("Increment")
    class IncrementTests {

        @Test
        @DisplayName("Casting a four-mana spell puts a +1/+1 counter on the 3/4")
        void fourManaSpellAddsCounter() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.BLUE, 4);
            harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Concentrate()));
            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Casting a one-mana spell does not put a counter on the 3/4")
        void oneManaSpellAddsNoCounter() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isZero();
        }
    }
}
