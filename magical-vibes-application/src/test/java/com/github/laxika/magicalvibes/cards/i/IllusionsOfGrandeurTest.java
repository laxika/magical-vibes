package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IllusionsOfGrandeurTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield gains 20 life")
    void entryGainsTwentyLife() {
        harness.setHand(player1, List.of(new IllusionsOfGrandeur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Leaving the battlefield loses 20 life")
    void leavingLosesTwentyLife() {
        harness.addToBattlefield(player1, new IllusionsOfGrandeur());
        harness.setLife(player1, 40);

        Permanent illusions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof IllusionsOfGrandeur)
                .findFirst().orElseThrow();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, illusions);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusions of Grandeur")
    void paysCumulativeUpkeep() {
        Permanent illusions = harness.addToBattlefieldAndReturn(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(illusions.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(illusions);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusions of Grandeur")
    void declineSacrifices() {
        Permanent illusions = harness.addToBattlefieldAndReturn(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(illusions);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusions of Grandeur"));
    }
}
