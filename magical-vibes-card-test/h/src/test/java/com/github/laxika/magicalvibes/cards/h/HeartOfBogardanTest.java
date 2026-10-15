package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartOfBogardan.class, BenalishKnight.class, ChandraNalaar.class})
class HeartOfBogardanTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Heart of Bogardan and deals no damage")
    void payingUpkeepKeepsIt() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(heart.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heart);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining on the first upkeep sacrifices it for 0 damage (twice one age counter minus 2)")
    void firstUpkeepDealsNoDamage() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
        harness.assertInGraveyard(player1, "Heart of Bogardan");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
    }

    @Test
    @DisplayName("Declining on the third upkeep deals 4 damage to the opponent and their creatures")
    void thirdUpkeepDealsFourDamage() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new BenalishKnight());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(heart.getCounterCount(CounterType.AGE)).isEqualTo(3);
        harness.handleMayAbilityChosen(player1, false);

        // X = 2 * 3 - 2 = 4: lethal to the 2/2 and 4 off the opponent's life total.
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
    }

    @Test
    @DisplayName("Declining cumulative upkeep prompts for a target, including the controller")
    void decliningUpkeepPromptsForTarget() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(heart.getCounterCount(CounterType.AGE)).isEqualTo(3);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Benalish Knight");
    }

    @Test
    @DisplayName("Unpaid second upkeep damages only the targeted player's creatures")
    void secondUpkeepDamagesOnlyTargetedPlayersCreatures() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        heart.setCounterCount(CounterType.AGE, 1);
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Heart of Bogardan");
        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownKnight);
        assertThat(ownKnight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Targeting a planeswalker damages it and its controller's creatures, but not that player")
    void targetsPlaneswalkerAndItsControllersCreatures() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        heart.setCounterCount(CounterType.AGE, 2);
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player2, new BenalishKnight());
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Benalish Knight");
        harness.assertInGraveyard(player1, "Heart of Bogardan");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownKnight);
        assertThat(ownKnight.getMarkedDamage()).isZero();
    }
}
