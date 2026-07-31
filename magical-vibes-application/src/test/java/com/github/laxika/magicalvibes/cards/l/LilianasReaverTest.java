package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard and creates a tapped 2/2 Zombie")
    void combatDamageDiscardsAndCreatesZombie() {
        addAttackingReaver(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Zombie"))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(2);
                    assertThat(p.getCard().getToughness()).isEqualTo(2);
                    assertThat(p.isTapped()).isTrue();
                });
    }

    @Test
    @DisplayName("Empty-handed damaged player still gives the controller a Zombie")
    void emptyHandStillCreatesZombie() {
        addAttackingReaver(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Zombie"))
                .hasSize(1);
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches the player")
    void noTriggerWhenBlocked() {
        addAttackingReaver(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    private Permanent addAttackingReaver(Player player) {
        Permanent reaver = addCreatureReady(player, new LilianasReaver());
        reaver.setAttacking(true);
        return reaver;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
