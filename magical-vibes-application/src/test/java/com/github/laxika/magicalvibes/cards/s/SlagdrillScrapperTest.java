package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlagdrillScrapper.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class SlagdrillScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact draws a card")
    void sacrificingArtifactDrawsCard() {
        addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        addActivationMana(player1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Sacrificing another land draws a card")
    void sacrificingLandDrawsCard() {
        addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        addActivationMana(player1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot sacrifice Slagdrill Scrapper itself")
    void cannotSacrificeSource() {
        addCreatureReady(player1, new SlagdrillScrapper());
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact or land");
    }

    @Test
    void costsArePaidBeforeDrawing() {
        Permanent scrapper = addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new SlagdrillScrapper()));
        harness.setHand(player1, List.of());
        addActivationMana(player1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);

        assertThat(scrapper.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Slagdrill Scrapper");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new Forest());
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent scrapper = addCreatureReady(player1, new SlagdrillScrapper());
        scrapper.tap();
        harness.addToBattlefield(player1, new Forest());
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOnlyOneMana() {
        addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeNonartifactCreature() {
        addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsPermanents() {
        addCreatureReady(player1, new SlagdrillScrapper());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new SlagdrillScrapper());
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Slagdrill Scrapper");
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
