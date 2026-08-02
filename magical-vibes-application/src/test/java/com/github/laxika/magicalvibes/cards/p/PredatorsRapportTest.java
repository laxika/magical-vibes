package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredatorsRapportTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new PredatorsRapport()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gains life equal to the target creature's power plus its toughness")
    void gainsLifeEqualToPowerPlusToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Grizzly Bears is 2/2 → 4 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Reads the specific chosen creature's stats")
    void readsChosenCreatureStats() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        // Hill Giant is 3/3 → 6 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        UUID opponentCreature = harness.getPermanentId(player2, "Hill Giant");
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
    }
}
