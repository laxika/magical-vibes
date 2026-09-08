package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BullElephant;
import com.github.laxika.magicalvibes.cards.k.KingCheetah;
import com.github.laxika.magicalvibes.cards.w.WickedReward;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgMindsucker.class, KingCheetah.class, BullElephant.class, WickedReward.class})
class UrborgMindsuckerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Urborg Mindsucker and targets the opponent")
    void activatingSacrificesAndTargetsOpponent() {
        addCreatureReady(player1, new UrborgMindsucker());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Urborg Mindsucker");
        harness.assertInGraveyard(player1, "Urborg Mindsucker");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving makes the target opponent discard a card at random")
    void discardsOneAtRandom() {
        addCreatureReady(player1, new UrborgMindsucker());
        harness.setHand(player2, List.of(new KingCheetah(), new BullElephant(), new WickedReward()));
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty hand does nothing on resolution")
    void emptyHandDoesNothing() {
        addCreatureReady(player1, new UrborgMindsucker());
        harness.setHand(player2, List.of());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Urborg Mindsucker");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addCreatureReady(player1, new UrborgMindsucker());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate at instant speed")
    void cannotActivateAtInstantSpeed() {
        addCreatureReady(player1, new UrborgMindsucker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate during upkeep")
    void cannotActivateDuringUpkeep() {
        addCreatureReady(player1, new UrborgMindsucker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate while the stack is not empty")
    void cannotActivateWithNonEmptyStack() {
        addCreatureReady(player1, new UrborgMindsucker());
        readyForSorcerySpeed();
        harness.setHand(player1, List.of(new KingCheetah()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Urborg Mindsucker");
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new UrborgMindsucker());
        readyForSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Urborg Mindsucker");
        harness.assertNotInGraveyard(player1, "Urborg Mindsucker");
        assertThat(gd.stack).isEmpty();
    }

    private void readyForSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
