package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.ManaMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromarTheBanisher.class, DreamThrush.class, Duskwalker.class, ManaMaze.class})
class DromarTheBanisherTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{U} returns every creature of the chosen color but not other permanents")
    void returnsCreaturesOfChosenColor() {
        Permanent dromar = addCreatureReady(player1, new DromarTheBanisher());
        dromar.setAttacking(true);
        harness.addToBattlefield(player2, new DreamThrush());
        harness.addToBattlefield(player2, new Duskwalker());
        harness.addToBattlefield(player2, new ManaMaze());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");

        harness.assertInHand(player1, "Dromar, the Banisher");
        harness.assertInHand(player2, "Dream Thrush");
        harness.assertOnBattlefield(player2, "Duskwalker");
        harness.assertOnBattlefield(player2, "Mana Maze");
    }

    @Test
    @DisplayName("Declining the combat-damage payment does nothing")
    void decliningPaymentDoesNothing() {
        Permanent dromar = addCreatureReady(player1, new DromarTheBanisher());
        dromar.setAttacking(true);
        harness.addToBattlefield(player2, new DreamThrush());

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Dromar, the Banisher");
        harness.assertOnBattlefield(player2, "Dream Thrush");
    }
}
