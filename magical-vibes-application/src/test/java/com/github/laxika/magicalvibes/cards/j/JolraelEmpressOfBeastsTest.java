package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JolraelEmpressOfBeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Animates all lands controlled by the targeted player and discards two cards")
    void animatesTargetPlayersLands() {
        readyJolrael();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent targetForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent targetMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateAgainst(player2);

        assertThat(targetForest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(targetForest.getEffectivePower()).isEqualTo(3);
        assertThat(targetForest.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.isCreature(gd, targetForest)).isTrue();
        assertThat(targetForest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(targetMountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, targetMountain)).isTrue();
        assertThat(ownLand.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(targetCreature.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target its controller's lands")
    void canTargetController() {
        readyJolrael();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        activateAgainst(player1);

        assertThat(gqs.isCreature(gd, ownLand)).isTrue();
        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        readyJolrael();
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        activateAgainst(player2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetLand.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, targetLand)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        readyJolrael();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyJolrael() {
        addCreatureReady(player1, new JolraelEmpressOfBeasts());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateAgainst(com.github.laxika.magicalvibes.model.Player target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
