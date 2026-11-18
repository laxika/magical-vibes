package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StampedeDriver.class, Mossdog.class})
class StampedeDriverTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card gives your creatures +1/+1 and trample until end of turn")
    void boostsOwnCreaturesAndGrantsTrample() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        Permanent otherCreature = addCreatureReady(player1, new Mossdog());
        Permanent opponentCreature = addCreatureReady(player2, new Mossdog());
        harness.setHand(player1, List.of(new Mossdog()));

        activateDriver(driver);

        assertThat(driver.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, driver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, driver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, driver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Mossdog");
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        Permanent otherCreature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new Mossdog()));

        activateDriver(driver);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, driver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, driver)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, driver, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not get the temporary boost")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        harness.setHand(player1, List.of(new Mossdog()));

        activateDriver(driver);

        Permanent lateCreature = addCreatureReady(player1, new Mossdog());

        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(driver.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the boost and trample")
    void affectsCreaturesEnteringBeforeResolution() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        harness.setHand(player1, List.of(new Mossdog()));

        activateDriverWithoutResolving(driver);

        assertThat(driver.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mossdog");
        assertThat(gqs.getEffectivePower(gd, driver)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, driver, Keyword.TRAMPLE)).isFalse();

        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent driver = addCreatureReady(player1, new StampedeDriver());
        driver.tap();
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mossdog");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent driver = harness.addToBattlefieldAndReturn(player1, new StampedeDriver());
        driver.setSummoningSick(true);
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(driver.isTapped()).isFalse();
        harness.assertInHand(player1, "Mossdog");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private void activateDriverWithoutResolving(Permanent driver) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(driver), null, null);
        harness.handleCardChosen(player1, 0);
    }

    private void activateDriver(Permanent driver) {
        activateDriverWithoutResolving(driver);
        harness.passBothPriorities();
    }
}
