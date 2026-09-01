package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalaGreeters.class, GrizzlyBears.class})
class GalaGreetersTest extends BaseCardTest {

    private static final String COUNTER = "Put a +1/+1 counter on this creature.";
    private static final String TREASURE = "Create a tapped Treasure token.";
    private static final String LIFE = "You gain 2 life.";

    @Test
    @DisplayName("Each Alliance mode can be chosen once per turn")
    void eachModeCanBeChosenOncePerTurn() {
        var greeters = harness.addToBattlefieldAndReturn(player1, new GalaGreeters());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        castCreatureAndChoose(COUNTER);
        castCreatureAndChoose(TREASURE);
        castCreatureAndChoose(LIFE);

        assertThat(greeters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("An Alliance trigger has no effect after all modes were chosen")
    void triggerHasNoEffectAfterAllModesWereChosen() {
        harness.addToBattlefield(player1, new GalaGreeters());
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        castCreatureAndChoose(COUNTER);
        castCreatureAndChoose(TREASURE);
        castCreatureAndChoose(LIFE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gala Greeters does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new GalaGreeters()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gala Greeters does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new GalaGreeters());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castCreatureAndChoose(String mode) {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
