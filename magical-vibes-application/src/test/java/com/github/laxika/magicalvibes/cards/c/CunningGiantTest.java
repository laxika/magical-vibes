package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningGiant.class, BearCub.class, Forest.class, ChandraNalaar.class})
class CunningGiantTest extends BaseCardTest {

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    @Test
    @DisplayName("Unblocked Cunning Giant may assign its combat damage to a defending creature")
    void redirectsDamageToDefendingCreature() {
        harness.setLife(player2, 20);
        Permanent giant = addReadyAttacker(player1, new CunningGiant());
        Permanent blocker = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // unblocked
        harness.passBothPriorities();

        // Assign all 4 combat damage to the defending Bear Cub
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Bear Cub");
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Unblocked Cunning Giant may still deal its combat damage to the player")
    void mayStillHitThePlayer() {
        harness.setLife(player2, 20);
        addReadyAttacker(player1, new CunningGiant());
        addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Cunning Giant cannot split its combat damage between recipients")
    void cannotSplitDamage() {
        harness.setLife(player2, 20);
        addReadyAttacker(player1, new CunningGiant());
        Permanent blocker = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblocked Cunning Giant may choose one of multiple defending creatures")
    void choosesOneOfMultipleDefendingCreatures() {
        harness.setLife(player2, 20);
        addReadyAttacker(player1, new CunningGiant());
        Permanent firstCreature = addCreatureReady(player2, new BearCub());
        Permanent secondCreature = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(secondCreature.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondCreature);
    }

    @Test
    @DisplayName("A blocked Cunning Giant assigns combat damage to its blocker")
    void blockedGiantDoesNotRedirectDamage() {
        harness.setLife(player2, 20);
        Permanent giant = addReadyAttacker(player1, new CunningGiant());
        addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Bear Cub");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
    }

    @Test
    @DisplayName("Unblocked Cunning Giant may assign damage to a creature while attacking a planeswalker")
    void redirectsDamageWhileAttackingPlaneswalker() {
        harness.setLife(player2, 20);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        Permanent giant = addReadyAttacker(player1, new CunningGiant());
        giant.setAttackTarget(planeswalker.getId());
        Permanent creature = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class))
                .isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(creature.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertInGraveyard(player2, "Bear Cub");
    }

    @Test
    @DisplayName("With no defending creatures, Cunning Giant deals its damage to the player with no prompt")
    void noPromptWithoutDefendingCreatures() {
        harness.setLife(player2, 20);
        addReadyAttacker(player1, new CunningGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A defending noncreature permanent does not enable Cunning Giant's choice")
    void noPromptWithOnlyNoncreaturePermanent() {
        harness.setLife(player2, 20);
        addReadyAttacker(player1, new CunningGiant());
        harness.addToBattlefield(player2, new Forest());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
