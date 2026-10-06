package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShoreKeeper.class})
class ShoreKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Shore Keeper and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addCreatureReady(player1, new ShoreKeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Shore Keeper should be sacrificed immediately (cost)
        harness.assertNotOnBattlefield(player1, "Shore Keeper");
        harness.assertInGraveyard(player1, "Shore Keeper");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Shore Keeper");
    }

    @Test
    @DisplayName("Resolving ability draws three cards")
    void resolvingAbilityDrawsThreeCards() {
        addCreatureReady(player1, new ShoreKeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new ShoreKeeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new ShoreKeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (requires tap)")
    void cannotActivateWithSummoningSickness() {
        ShoreKeeper card = new ShoreKeeper();
        harness.addToBattlefield(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate an already tapped Shore Keeper")
    void cannotActivateWhileTapped() {
        Permanent permanent = addCreatureReady(player1, new ShoreKeeper());
        permanent.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shore Keeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Seven total mana cannot pay the activation cost even with blue mana")
    void cannotActivateWithOnlySevenMana() {
        Permanent permanent = addCreatureReady(player1, new ShoreKeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shore Keeper");
        assertThat(permanent.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cards are drawn only on resolution and only by the ability's controller")
    void drawsOnlyForControllerOnResolution() {
        addCreatureReady(player2, new ShoreKeeper());
        harness.setLibrary(player2, List.of(new ShoreKeeper(), new ShoreKeeper(), new ShoreKeeper()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.addMana(player2, ManaColor.BLUE, 1);
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentHandSize = gd.playerHands.get(player1.getId()).size();
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, null);

        harness.assertInGraveyard(player2, "Shore Keeper");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
