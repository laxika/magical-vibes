package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyphonLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Syphon Life makes target player lose 2 life and controller gain 2 life")
    void drainsTwoAndGainsTwo() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Syphon Life cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Retrace lets Syphon Life be recast from the graveyard by discarding a land")
    void retraceDiscardsLandAndDrains() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new SyphonLife()));
        harness.setHand(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Swamp"));
    }

    @Test
    @DisplayName("Retrace returns Syphon Life to the graveyard, not exile")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new SyphonLife()));
        harness.setHand(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Syphon Life"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Syphon Life"));
    }
}
