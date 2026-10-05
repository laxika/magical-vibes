package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({MessengerHawk.class, Island.class})
class MessengerHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue when it enters the battlefield")
    void createsClueOnEntering() {
        harness.enterBattlefieldAndReturn(player1, new MessengerHawk());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Gets +2/+0 after its controller draws two cards")
    void gainsPowerAfterControllerDrawsTwoCards() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new MessengerHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);

        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's draws do not grant the power bonus")
    void opponentDrawsDoNotGrantPowerBonus() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new MessengerHawk());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        draw(player2);
        draw(player2);

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Clue can be sacrificed for two mana to draw the second card")
    void clueDrawEnablesPowerBonus() {
        Permanent hawk = harness.enterBattlefieldAndReturn(player1, new MessengerHawk());
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws before entering count and further draws do not stack the bonus")
    void countsEarlierDrawsWithoutStackingBonus() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        draw(player1);
        draw(player1);

        Permanent hawk = harness.enterBattlefieldAndReturn(player1, new MessengerHawk());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("The power bonus resets on the next turn")
    void powerBonusResetsNextTurn() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new MessengerHawk());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
