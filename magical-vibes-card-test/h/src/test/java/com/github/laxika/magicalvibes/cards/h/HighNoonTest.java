package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighNoon.class, GrizzlyBears.class})
class HighNoonTest extends BaseCardTest {

    @Test
    @DisplayName("Each player can cast only one spell each turn")
    void limitsEachPlayerToOneSpellEachTurn() {
        harness.addToBattlefield(player1, new HighNoon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The spell limit also applies to an opponent")
    void limitsOpponentToOneSpellEachTurn() {
        harness.addToBattlefield(player1, new HighNoon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices itself and deals 5 damage to a player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new HighNoon());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "High Noon");
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Sacrifices itself and deals 5 damage to a creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new HighNoon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "High Noon");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting High Noon uses the spell allowance for that turn")
    void highNoonItselfCountsAsTheSpellForTheTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HighNoon(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "High Noon");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spells cast before High Noon enters still count toward its limit")
    void countsSpellsCastBeforeItEnters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new HighNoon());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damage ability can be activated after casting a spell and sacrifice lifts the limit")
    void sacrificeLiftsTheLimitForTheSameTurn() {
        harness.addToBattlefield(player1, new HighNoon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "High Noon");
        harness.assertInGraveyard(player1, "High Noon");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activation requires red mana and cannot sacrifice High Noon without paying")
    void cannotActivateWithOnlyGenericMana() {
        harness.addToBattlefield(player1, new HighNoon());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "High Noon");
        harness.assertNotInGraveyard(player1, "High Noon");
        harness.assertLife(player2, 20);
    }
}
