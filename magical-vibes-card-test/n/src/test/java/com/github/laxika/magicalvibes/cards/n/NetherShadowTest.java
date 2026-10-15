package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetherShadow.class, GrizzlyBears.class, DarkRitual.class})
class NetherShadowTest extends BaseCardTest {

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
    @DisplayName("Triggers with more than three creature cards above it")
    void triggersWithMoreThanThreeCreaturesAbove() {
        NetherShadow shadow = new NetherShadow();
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
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

        harness.assertOnBattlefield(player1, "Nether Shadow");
        harness.assertNotInGraveyard(player1, "Nether Shadow");
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

        harness.assertNotOnBattlefield(player1, "Nether Shadow");
        harness.assertInGraveyard(player1, "Nether Shadow");
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
                new DarkRitual(), new DarkRitual(), new DarkRitual()));

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

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.setGraveyard(player1, List.of(new NetherShadow(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not return when fewer than three creatures remain as the trigger resolves")
    void conditionMustStillHoldWhenTriggerResolves() {
        NetherShadow shadow = new NetherShadow();
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(shadow,
                new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Nether Shadow");
        harness.assertInGraveyard(player1, "Nether Shadow");
    }

    @Test
    @DisplayName("Interspersed noncreature cards do not prevent three creatures above from counting")
    void countsCreaturesSeparatedByNoncreatures() {
        harness.setGraveyard(player1, List.of(new NetherShadow(),
                new GrizzlyBears(), new DarkRitual(), new GrizzlyBears(),
                new DarkRitual(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Nether Shadow");
        harness.assertNotInGraveyard(player1, "Nether Shadow");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Removing the source before resolution prevents its return")
    void doesNotReturnWhenSourceLeavesGraveyard() {
        NetherShadow shadow = new NetherShadow();
        List<Card> creatures = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shadow,
                creatures.get(0), creatures.get(1), creatures.get(2)));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, creatures);
        harness.setHand(player1, List.of(shadow));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertNotOnBattlefield(player1, "Nether Shadow");
        harness.assertNotInGraveyard(player1, "Nether Shadow");
    }

    @Test
    @DisplayName("An old upkeep trigger cannot return a Shadow that left and reentered the graveyard")
    void doesNotReturnNewGraveyardObject() {
        NetherShadow shadow = new NetherShadow();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shadow, first, second, third));
        gd.markGraveyardEntry(shadow);

        advanceToUpkeep(player1);
        // Model a round trip through hand followed by three creatures entering above it.
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(shadow));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(shadow, first, second, third));
        gd.markGraveyardEntry(shadow);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Nether Shadow");
        harness.assertInGraveyard(player1, "Nether Shadow");
    }

    @Test
    @DisplayName("A returned Shadow can attack during the same turn")
    void returnedShadowCanAttackImmediately() {
        harness.setGraveyard(player1, List.of(new NetherShadow(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Each qualifying Shadow returns only itself")
    void multipleShadowsReturnIndependently() {
        NetherShadow bottom = new NetherShadow();
        NetherShadow upper = new NetherShadow();
        harness.setGraveyard(player1, List.of(bottom, upper,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        UUID firstId = gd.pendingMayAbilities.getFirst().sourceCard().getId();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getId()).isEqualTo(firstId);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bottom.getId(), upper.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
