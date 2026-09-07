package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFlectomancer.class, LavaAxe.class, GrizzlyBears.class})
class GoblinFlectomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Goblin Flectomancer targets an instant or sorcery spell")
    void activationTargetsInstantOrSorcerySpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, lavaAxe.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(lavaAxe.getId());
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(flectomancer).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Goblin Flectomancer cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flectomancer);
    }

    @Test
    @DisplayName("Accepting Goblin Flectomancer's may ability changes the spell's target")
    void acceptingRetargetChangesSpellTarget() {
        addCreatureReady(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, lavaAxe.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        StackEntry lavaAxeEntry = gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(lavaAxe.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(lavaAxeEntry.getTargetId()).isEqualTo(player1.getId());

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }

    @Test
    @DisplayName("Declining Goblin Flectomancer's may ability keeps the original target")
    void decliningRetargetKeepsOriginalTarget() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, lavaAxe.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }
}
