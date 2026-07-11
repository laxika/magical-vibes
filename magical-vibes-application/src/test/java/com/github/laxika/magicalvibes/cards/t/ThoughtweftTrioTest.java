package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtweftTrioTest extends BaseCardTest {

    private void castThoughtweftTrio() {
        harness.setHand(player1, List.of(new ThoughtweftTrio()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    // ===== Champion a Kithkin =====

    @Test
    @DisplayName("Auto-sacrifices when controller has no other Kithkin")
    void autoSacrificesWithNoOtherKithkin() {
        castThoughtweftTrio();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thoughtweft Trio"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thoughtweft Trio"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Auto-sacrifices when only a non-Kithkin creature is present")
    void autoSacrificesWithOnlyNonKithkin() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castThoughtweftTrio();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thoughtweft Trio"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB with another Kithkin prompts champion choice")
    void etbWithAnotherKithkinPromptsChoice() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thoughtweft Trio"));
    }

    @Test
    @DisplayName("Championing a Kithkin exiles it and keeps Thoughtweft Trio")
    void championingExilesKithkinAndKeepsTrio() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities();

        UUID stalwartId = harness.getPermanentId(player1, "Goldmeadow Stalwart");
        harness.handlePermanentChosen(player1, stalwartId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thoughtweft Trio"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Championed Kithkin returns when Thoughtweft Trio leaves the battlefield")
    void championedKithkinReturnsWhenTrioLeaves() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities();

        UUID stalwartId = harness.getPermanentId(player1, "Goldmeadow Stalwart");
        harness.handlePermanentChosen(player1, stalwartId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID trioId = harness.getPermanentId(player1, "Thoughtweft Trio");
        harness.castInstant(player1, 0, trioId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thoughtweft Trio"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    // ===== Can block any number of creatures =====

    @Test
    @DisplayName("Thoughtweft Trio can block three attackers at once")
    void canBlockThreeAttackers() {
        Permanent trioPerm = new Permanent(new ThoughtweftTrio());
        trioPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(trioPerm);

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ));

        assertThat(trioPerm.isBlocking()).isTrue();
        assertThat(trioPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }
}
