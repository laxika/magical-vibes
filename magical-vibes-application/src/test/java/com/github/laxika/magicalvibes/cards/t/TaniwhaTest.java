package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Taniwha.class, Island.class, Forest.class, IronTuskElephant.class, Boomerang.class})
class TaniwhaTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, all lands you control phase out")
    void ownLandsPhaseOut() {
        harness.addToBattlefield(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Non-land permanents you control and opponent lands are unaffected")
    void onlyOwnLandsAffected() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new IronTuskElephant());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taniwha, elephant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentIsland);
    }

    @Test
    @DisplayName("The phased-out lands phase in during the controller's next untap step")
    void landsPhaseBackIn() {
        harness.addToBattlefield(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);

        advanceTurn(); // player2's turn — still phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);

        advanceTurn(); // back to player1's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UNTAP);
    }

    @Test
    @DisplayName("Taniwha phases out before upkeep and does not trigger while phased out")
    void phasedOutTaniwhaDoesNotTrigger() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(taniwha);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taniwha does not phase out its controller's lands during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taniwha, island);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability phases out lands present at resolution, not just at trigger time")
    void includesLandsEnteringBeforeResolution() {
        harness.addToBattlefield(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        assertThat(gd.stack).hasSize(1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Taniwha tramples over a blocker after assigning lethal damage")
    void trampleDealsExcessDamage() {
        Permanent taniwha = addCreatureReady(player1, new Taniwha());
        Permanent elephant = addCreatureReady(player2, new IronTuskElephant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(elephant.getId(), 3, player2.getId(), 4));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elephant);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taniwha);
        harness.assertLife(player2, 16);
    }

    private void advanceToUpkeepWithTaniwhaPhasedIn() {
        advanceToUpkeep(player1);
        advanceToUpkeep(player1);
    }

    @Test
    @DisplayName("Tapped lands return before untapping while Taniwha phases out simultaneously")
    void tappedLandReturnsUntapped() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        island.tap();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);
        assertThat(island.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island).doesNotContain(taniwha);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(taniwha).doesNotContain(island);
        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The upkeep ability still phases out lands after Taniwha leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Boomerang()));

        advanceToUpkeepWithTaniwhaPhasedIn();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, taniwha.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(taniwha);
        assertThat(gd.playerHands.get(player1.getId())).contains(taniwha.getCard());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);
    }
}
