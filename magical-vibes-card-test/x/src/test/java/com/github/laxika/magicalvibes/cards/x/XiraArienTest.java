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

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent xira = harness.addToBattlefieldAndReturn(player1, new XiraArien());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(xira.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draw happens on resolution, after mana and tap costs are paid")
    void drawWaitsForResolution() {
        Permanent xira = addReadyXira(player1);
        AvoidFate topCard = new AvoidFate();
        AvoidFate nextCard = new AvoidFate();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        addActivationMana();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(xira.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Can activate during the opponent's turn")
    void canActivateDuringOpponentsTurn() {
        addReadyXira(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setLibrary(player1, List.of(new AvoidFate()));
        addActivationMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Avoid Fate");
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
