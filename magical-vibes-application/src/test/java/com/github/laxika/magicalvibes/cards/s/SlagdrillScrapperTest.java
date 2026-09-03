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
        addReadyScrapper(player1);
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
        addReadyScrapper(player1);
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
        addReadyScrapper(player1);
        addActivationMana(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact or land");
    }

    private Permanent addReadyScrapper(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SlagdrillScrapper());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
