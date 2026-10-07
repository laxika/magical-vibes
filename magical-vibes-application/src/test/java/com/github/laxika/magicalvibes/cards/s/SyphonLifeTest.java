package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FetidHeath;
import com.github.laxika.magicalvibes.model.GameStatus;
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

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Syphon Life can target its controller")
    void canTargetController() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
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

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 18);
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

    @Test
    @DisplayName("Syphon Life can be retraced repeatedly with a new land discard each time")
    void canRetraceRepeatedly() {
        SyphonLife spell = new SyphonLife();
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new FetidHeath(), new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        int spellIndex = gd.playerGraveyards.get(player1.getId()).indexOf(spell);
        assertThat(spellIndex).isGreaterThanOrEqualTo(0);
        harness.castRetrace(player1, spellIndex, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Fetid Heath")).hasSize(2);
        harness.assertInGraveyard(player1, "Syphon Life");
    }

    @Test
    @DisplayName("Retrace still requires Syphon Life's full mana cost")
    void retraceRequiresManaInAdditionToLand() {
        SyphonLife spell = new SyphonLife();
        FetidHeath land = new FetidHeath();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Self-targeted Syphon Life restores life before state-based actions")
    void selfTargetAtTwoLifeSurvivesResolution() {
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new SyphonLife()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Syphon Life");
    }
}
