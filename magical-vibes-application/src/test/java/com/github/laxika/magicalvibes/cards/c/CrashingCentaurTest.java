package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrashingCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, Discard a card gives this creature trample until end of turn")
    void discardGrantsTrample() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new CrashingCentaur());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, centaur, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold gives this creature +2/+2 and shroud")
    void thresholdBonus() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new CrashingCentaur());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, centaur, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Threshold does not count an opponent's graveyard")
    void opponentGraveyardDoesNotCount() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new CrashingCentaur());
        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, centaur, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The threshold bonus disappears below seven cards in the graveyard")
    void thresholdBonusDisappears() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new CrashingCentaur());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, centaur, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new CrashingCentaur());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
