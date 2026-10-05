package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cryptex;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PublicThoroughfare.class, Forest.class, Cryptex.class})
class PublicThoroughfareTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playPublicThoroughfare();

        Permanent thoroughfare = findThoroughfare(player1);
        assertThat(thoroughfare).isNotNull();
        assertThat(thoroughfare.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping an untapped land keeps Public Thoroughfare on the battlefield")
    void tappingLandKeepsIt() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        playPublicThoroughfare();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        assertThat(findThoroughfare(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Tapping an untapped artifact keeps Public Thoroughfare on the battlefield")
    void tappingArtifactKeepsIt() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());

        playPublicThoroughfare();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptex.isTapped()).isTrue();
        assertThat(findThoroughfare(player1)).isNotNull();
    }

    @Test
    @DisplayName("Declining to tap an artifact or land sacrifices Public Thoroughfare")
    void decliningTapSacrificesIt() {
        playPublicThoroughfare();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findThoroughfare(player1)).isNull();
        harness.assertInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("May decline payment even when an untapped land is available")
    void mayDeclineAvailablePayment() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        playPublicThoroughfare();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Public Thoroughfare");
        harness.assertInGraveyard(player1, "Public Thoroughfare");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay with Public Thoroughfare when it is the only land")
    void cannotPayWithTappedSource() {
        playPublicThoroughfare();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Public Thoroughfare");
        harness.assertInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Tapped lands and artifacts cannot pay the entry cost")
    void tappedPermanentsCannotPay() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Cryptex()).tap();

        playPublicThoroughfare();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Public Thoroughfare");
        harness.assertInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Opponent's lands and artifacts cannot pay the entry cost")
    void opposingPermanentsCannotPay() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent cryptex = harness.addToBattlefieldAndReturn(player2, new Cryptex());

        playPublicThoroughfare();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Public Thoroughfare");
        assertThat(forest.isTapped()).isFalse();
        assertThat(cryptex.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the chosen permanent is tapped when several can pay")
    void choosesOnePermanentToTap() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());

        playPublicThoroughfare();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, cryptex.getId());

        assertThat(cryptex.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Public Thoroughfare");
        harness.assertNotInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("An untapped Public Thoroughfare can pay its own entry cost")
    void untappedSourceCanPay() {
        playPublicThoroughfare();
        Permanent thoroughfare = findThoroughfare(player1);
        thoroughfare.untap();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thoroughfare.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Public Thoroughfare");
        harness.assertNotInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent thoroughfare = addThoroughfareReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(thoroughfare.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playPublicThoroughfare() {
        harness.setHand(player1, List.of(new PublicThoroughfare()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addThoroughfareReady(Player player) {
        Permanent thoroughfare = harness.addToBattlefieldAndReturn(player, new PublicThoroughfare());
        thoroughfare.setSummoningSick(false);
        return thoroughfare;
    }

    private Permanent findThoroughfare(Player player) {
        return findPermanents(player, "Public Thoroughfare").stream().findFirst().orElse(null);
    }
}
