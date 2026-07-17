package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetherShadowTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Triggers with three creature cards above it in the graveyard")
    void triggersWithThreeCreaturesAbove() {
        NetherShadow shadow = new NetherShadow();
        // Bottom to top: Nether Shadow first, then three creatures above it.
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getId()).isEqualTo(shadow.getId());
    }

    @Test
    @DisplayName("Accepting the trigger puts Nether Shadow onto the battlefield")
    void acceptPutsOntoBattlefield() {
        NetherShadow shadow = new NetherShadow();
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(shadow.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(shadow.getId()));
    }

    @Test
    @DisplayName("Declining keeps Nether Shadow in the graveyard")
    void declineKeepsInGraveyard() {
        NetherShadow shadow = new NetherShadow();
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(shadow.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(shadow.getId()));
    }

    @Test
    @DisplayName("Does not trigger with only two creature cards above it")
    void doesNotTriggerWithTwoCreaturesAbove() {
        harness.setGraveyard(player1, List.of(new NetherShadow(),
                new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-creature cards above it do not count toward the threshold")
    void nonCreatureCardsAboveDoNotCount() {
        harness.setGraveyard(player1, List.of(new NetherShadow(),
                new Shock(), new Shock(), new Shock()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Creature cards below it in the graveyard do not count")
    void creaturesBelowDoNotCount() {
        // Three creatures below Nether Shadow, none above → no trigger.
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new NetherShadow()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
