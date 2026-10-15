package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.d.Dawnfluke;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtweftTrio.class, GoldmeadowStalwart.class, Dawnfluke.class, CribSwap.class})
class ThoughtweftTrioTest extends BaseCardTest {

    private void castThoughtweftTrio() {
        harness.castFromHand(player1, new ThoughtweftTrio(), "{2}{W}{W}");
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other Kithkin")
    void autoSacrificesWithNoOtherKithkin() {
        castThoughtweftTrio();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Thoughtweft Trio");
        harness.assertInGraveyard(player1, "Thoughtweft Trio");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Auto-sacrifices when only a non-Kithkin creature is present")
    void autoSacrificesWithOnlyNonKithkin() {
        harness.addToBattlefield(player1, new Dawnfluke());
        castThoughtweftTrio();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thoughtweft Trio");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB with another Kithkin prompts champion choice")
    void etbWithAnotherKithkinPromptsChoice() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Thoughtweft Trio");
    }

    @Test
    @DisplayName("Championing a Kithkin exiles it and keeps Thoughtweft Trio")
    void championingExilesKithkinAndKeepsTrio() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities();

        UUID stalwartId = harness.getPermanentId(player1, "Goldmeadow Stalwart");
        harness.handlePermanentChosen(player1, stalwartId);

        harness.assertOnBattlefield(player1, "Thoughtweft Trio");
        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
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
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID trioId = harness.getPermanentId(player1, "Thoughtweft Trio");
        harness.castAndResolveInstant(player1, 0, trioId);

        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
        harness.passBothPriorities(); // resolve the champion leaves-the-battlefield trigger

        harness.assertNotOnBattlefield(player1, "Thoughtweft Trio");
        harness.assertOnBattlefield(player1, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Champion can exile a Kithkin permanently if Trio leaves before its entry trigger resolves")
    void championCanExileKithkinAfterTrioLeaves() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();

        UUID trioId = harness.getPermanentId(player1, "Thoughtweft Trio");
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, trioId);
        harness.passBothPriorities(); // resolve the champion entry trigger
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Goldmeadow Stalwart"));

        harness.assertNotOnBattlefield(player1, "Thoughtweft Trio");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thoughtweft Trio"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Controller may decline champion and sacrifice Trio even with a Kithkin available")
    void mayDeclineChampionWithKithkinAvailable() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Thoughtweft Trio");
        harness.assertNotOnBattlefield(player1, "Thoughtweft Trio");
        harness.assertOnBattlefield(player1, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Kithkin cannot be championed")
    void opponentKithkinCannotBeChampioned() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        castThoughtweftTrio();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thoughtweft Trio");
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The changeling token created by Crib Swap can be championed after Trio leaves")
    void changelingTokenCanBeChampionedAfterTrioLeaves() {
        castThoughtweftTrio();
        UUID trioId = harness.getPermanentId(player1, "Thoughtweft Trio");
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, trioId);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Thoughtweft Trio can block three attackers at once")
    void canBlockThreeAttackers() {
        Permanent trioPerm = addCreatureReady(player2, new ThoughtweftTrio());

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GoldmeadowStalwart());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ));

        assertThat(trioPerm.isBlocking()).isTrue();
        assertThat(trioPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }
}
