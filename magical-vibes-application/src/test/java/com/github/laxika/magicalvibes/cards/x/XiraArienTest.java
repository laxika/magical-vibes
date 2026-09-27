package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.a.AvoidFate;
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

@CardUsed({XiraArien.class, AvoidFate.class})
class XiraArienTest extends BaseCardTest {

    @Test
    @DisplayName("Pays mana and taps to make a target player draw a card")
    void targetPlayerDrawsCard() {
        Permanent xira = addReadyXira(player1);
        harness.setLibrary(player2, List.of(new AvoidFate()));
        addActivationMana();

        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(xira.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player2, "Avoid Fate");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        addReadyXira(player1);
        harness.setLibrary(player1, List.of(new AvoidFate()));
        addActivationMana();

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Avoid Fate");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyXira(player1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new XiraArien());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent xira = addReadyXira(player1);
        xira.tap();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without all three colored mana")
    void cannotActivateWithoutAllColoredMana() {
        Permanent xira = addReadyXira(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(xira.isTapped()).isFalse();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addReadyXira(Player player) {
        Permanent perm = addCreatureReady(player, new XiraArien());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
