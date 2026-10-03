package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CommandBridge.class, Forest.class})
class CommandBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playCommandBridge();

        Permanent bridge = findCommandBridge(player1);
        assertThat(bridge).isNotNull();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping an untapped permanent keeps Command Bridge on the battlefield")
    void tappingPermanentKeepsIt() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        playCommandBridge();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        assertThat(findCommandBridge(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Command Bridge");
    }

    @Test
    @DisplayName("Declining to tap a permanent sacrifices Command Bridge")
    void decliningTapSacrificesIt() {
        playCommandBridge();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findCommandBridge(player1)).isNull();
        harness.assertInGraveyard(player1, "Command Bridge");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent bridge = addCommandBridgeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining with an eligible permanent still sacrifices Command Bridge")
    void decliningWithEligiblePermanentSacrificesIt() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        playCommandBridge();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forest.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Command Bridge");
        harness.assertInGraveyard(player1, "Command Bridge");
    }

    @Test
    @DisplayName("An already tapped permanent cannot pay the enter trigger cost")
    void tappedPermanentCannotPay() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        playCommandBridge();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Command Bridge");
        harness.assertInGraveyard(player1, "Command Bridge");
    }

    @Test
    @DisplayName("An opponent's untapped permanent cannot pay the enter trigger cost")
    void opponentsPermanentCannotPay() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        playCommandBridge();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Command Bridge");
        harness.assertInGraveyard(player1, "Command Bridge");
    }

    @Test
    @DisplayName("The controller chooses exactly one of multiple eligible permanents to tap")
    void choosesOnePermanentToTap() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        playCommandBridge();
        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Command Bridge");
        harness.assertNotInGraveyard(player1, "Command Bridge");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Command Bridge can pay its own enter trigger cost if untapped before resolution")
    void untappedBridgeCanPayForItself() {
        playCommandBridge();
        Permanent bridge = findCommandBridge(player1);
        bridge.untap();

        resolveEnterTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bridge.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Command Bridge");
        harness.assertNotInGraveyard(player1, "Command Bridge");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playCommandBridge() {
        harness.setHand(player1, List.of(new CommandBridge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addCommandBridgeReady(Player player) {
        Permanent bridge = harness.addToBattlefieldAndReturn(player, new CommandBridge());
        bridge.setSummoningSick(false);
        return bridge;
    }

    private Permanent findCommandBridge(Player player) {
        return findPermanents(player, "Command Bridge").stream().findFirst().orElse(null);
    }
}
