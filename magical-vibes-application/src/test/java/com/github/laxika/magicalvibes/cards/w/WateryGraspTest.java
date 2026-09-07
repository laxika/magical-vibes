package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WateryGrasp.class, GrizzlyBears.class})
class WateryGraspTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature does not untap while Watery Grasp remains attached")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        attachAura(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend shuffles the enchanted creature into its owner's library")
    void waterbendShufflesEnchantedCreatureIntoOwnersLibrary() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent third = addCreatureReady(player1);
        Permanent fourth = addCreatureReady(player1);
        Permanent fifth = addCreatureReady(player1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(fourth.isTapped()).isTrue();
        assertThat(fifth.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Waterbend cannot be paid without five generic payments")
    void waterbendRequiresFivePayments() {
        Permanent aura = attachAura(player1, addCreatureReady(player2));
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent third = addCreatureReady(player1);
        Permanent fourth = addCreatureReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(fourth.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new WateryGrasp());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
