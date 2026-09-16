package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(ArcanisTheOmnipotent.class)
class ArcanisTheOmnipotentTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws three cards for its controller")
    void tapAbilityDrawsThreeCardsForItsController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(
                new ArcanisTheOmnipotent(), new ArcanisTheOmnipotent(), new ArcanisTheOmnipotent()));
        harness.setLibrary(player2, List.of(new ArcanisTheOmnipotent()));
        Permanent arcanis = addCreatureReady(player1, new ArcanisTheOmnipotent());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(arcanis.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tap ability requires Arcanis to be untapped and free of summoning sickness")
    void tapAbilityRequiresUntappedReadyCreature() {
        Permanent arcanis = harness.addToBattlefieldAndReturn(player1, new ArcanisTheOmnipotent());
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        arcanis.setSummoningSick(false);
        arcanis.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Mana ability returns Arcanis to its owner's hand even while tapped")
    void manaAbilityReturnsArcanisToOwnersHandEvenWhileTapped() {
        ArcanisTheOmnipotent card = new ArcanisTheOmnipotent();
        card.setOwnerId(player1.getId());
        Permanent arcanis = harness.addToBattlefieldAndReturn(player2, card);
        arcanis.setSummoningSick(false);
        arcanis.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        forceMainPhase(player2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
