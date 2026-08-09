package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StampedeDriverTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card gives your creatures +1/+1 and trample until end of turn")
    void boostsOwnCreaturesAndGrantsTrample() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activateDriver(driver);

        assertThat(driver.getEffectivePower()).isEqualTo(2);
        assertThat(driver.getEffectiveToughness()).isEqualTo(2);
        assertThat(driver.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(otherCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activateDriver(driver);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(driver.getEffectivePower()).isEqualTo(1);
        assertThat(driver.getEffectiveToughness()).isEqualTo(1);
        assertThat(driver.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(otherCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateDriver(Permanent driver) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(driver), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
