package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.p.PitImp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommanderGrevenIlVec.class, BottleGnomes.class, LotusPetal.class, LowlandGiant.class,
        PitImp.class})
class CommanderGrevenIlVecTest extends BaseCardTest {

    // "When Commander Greven il-Vec enters, sacrifice a creature."

    @Test
    @DisplayName("Alone on the battlefield, Greven sacrifices itself")
    void aloneSacrificesItself() {
        harness.castFromHand(player1, new CommanderGrevenIlVec(), "{3}{B}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertInGraveyard(player1, "Commander Greven il-Vec");
    }

    @Test
    @DisplayName("With another creature, controller is prompted and may spare Greven")
    void controllerChoosesWhichCreatureToSacrifice() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandGiant());
        harness.addToBattlefield(player1, new PitImp());

        harness.castFromHand(player1, new CommanderGrevenIlVec(), "{3}{B}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Lowland Giant");
        harness.assertOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertOnBattlefield(player1, "Pit Imp");
    }

    @Test
    @DisplayName("Controller may sacrifice Greven itself even with other creatures available")
    void mayChooseGrevenItself() {
        harness.addToBattlefield(player1, new LowlandGiant());

        harness.castFromHand(player1, new CommanderGrevenIlVec(), "{3}{B}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Commander Greven il-Vec"));

        harness.assertNotOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertInGraveyard(player1, "Commander Greven il-Vec");
        harness.assertOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Opponent's creatures are never sacrificed")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.addToBattlefield(player1, new LowlandGiant());

        harness.castFromHand(player1, new CommanderGrevenIlVec(), "{3}{B}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Lowland Giant"));

        harness.assertOnBattlefield(player2, "Bottle Gnomes");
        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertOnBattlefield(player1, "Commander Greven il-Vec");
    }

    @Test
    @DisplayName("Only creatures are eligible for the enters-the-battlefield sacrifice")
    void onlyCreaturesAreEligibleForSacrifice() {
        harness.addToBattlefield(player1, new LotusPetal());

        harness.castFromHand(player1, new CommanderGrevenIlVec(), "{3}{B}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertInGraveyard(player1, "Commander Greven il-Vec");
        harness.assertOnBattlefield(player1, "Lotus Petal");
    }

    @Test
    @DisplayName("Fear prevents a nonblack nonartifact creature from blocking Greven")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent greven = addCreatureReady(player1, new CommanderGrevenIlVec());
        Permanent blocker = addCreatureReady(player2, new LowlandGiant());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(greven)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block Greven")
    void fearAllowsBlackAndArtifactBlockers() {
        Permanent greven = addCreatureReady(player1, new CommanderGrevenIlVec());
        Permanent artifactBlocker = addCreatureReady(player2, new BottleGnomes());
        Permanent blackBlocker = addCreatureReady(player2, new PitImp());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(greven)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(greven))));

        assertThat(artifactBlocker.isBlocking()).isTrue();
        assertThat(blackBlocker.isBlocking()).isTrue();
    }
}
