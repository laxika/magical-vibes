package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NicolBolasTheDeceiverTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Lose 3 life";

    @Test
    @DisplayName("+3 forces opponent with no options to lose 3 life once")
    void plusThreeLosesLifeWhenNoOtherOption() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+3 lets opponent sacrifice a nonland permanent instead of losing life")
    void plusThreeOpponentMaySacrifice() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bearId);

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+3 lets opponent discard instead of losing life")
    void plusThreeOpponentMayDiscard() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+3 lets opponent choose to lose life even with other options")
    void plusThreeOpponentMayChooseToLoseLife() {
        addReadyBolas(player1, 5);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleListChoice(player2, LOSE_LIFE);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("−3 destroys target creature and draws a card")
    void minusThreeDestroysCreatureAndDraws() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, bearId);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("−3 cannot target a noncreature permanent")
    void minusThreeCannotTargetNoncreature() {
        addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−11 deals 7 damage to each opponent and draws seven cards")
    void minusElevenDamagesOpponentsAndDrawsSeven() {
        Permanent bolas = addReadyBolas(player1, 11);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, new ArrayList<>(IntStream.range(0, 7)
                .mapToObj(i -> new Forest())
                .toList()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot use −11 when loyalty is insufficient")
    void cannotActivateMinusElevenWithInsufficientLoyalty() {
        addReadyBolas(player1, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyBolas(Player player, int loyalty) {
        NicolBolasTheDeceiver card = new NicolBolasTheDeceiver();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
