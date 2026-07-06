package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConciliatorsDuelistTest extends BaseCardTest {

    private Permanent addReadyDuelist(Player player) {
        Permanent perm = new Permanent(new ConciliatorsDuelist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== ETB: draw + each player loses 1 life =====

    @Test
    @DisplayName("ETB draws a card for the controller and makes each player lose 1 life")
    void etbDrawsAndDrainsEachPlayer() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConciliatorsDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB triggers

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    // ===== Repartee: exile a target creature =====

    @Test
    @DisplayName("Casting an instant that targets a creature exiles a chosen creature and schedules its return")
    void reparteeExilesChosenCreature() {
        addReadyDuelist(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, giantId);

        // Repartee triggers and prompts for a creature to exile
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.pendingExileReturns)
                .anyMatch(per -> per.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        addReadyDuelist(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
}
