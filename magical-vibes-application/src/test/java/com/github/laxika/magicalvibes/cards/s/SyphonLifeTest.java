package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FetidHeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyphonLife.class, StalkerHag.class, FetidHeath.class})
class SyphonLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Syphon Life makes target player lose 2 life and controller gain 2 life")
    void drainsTwoAndGainsTwo() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Syphon Life can target its controller")
    void canTargetController() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Syphon Life cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StalkerHag());

        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Retrace lets Syphon Life be recast from the graveyard by discarding a land")
    void retraceDiscardsLandAndDrains() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new SyphonLife()));
        harness.setHand(player1, List.of(new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fetid Heath");
    }

    @Test
    @DisplayName("Retrace returns Syphon Life to the graveyard, not exile")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new SyphonLife()));
        harness.setHand(player1, List.of(new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Syphon Life");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Syphon Life"));
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new SyphonLife()));
        harness.setHand(player1, List.of(new StalkerHag()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
