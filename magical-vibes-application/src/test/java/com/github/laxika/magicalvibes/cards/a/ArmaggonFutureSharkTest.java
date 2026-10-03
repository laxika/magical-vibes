package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmaggonFutureShark.class, GrizzlyBears.class, Island.class})
class ArmaggonFutureSharkTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to three target creatures when it enters")
    void destroysThreeTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArmaggon(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(3);
    }

    @Test
    @DisplayName("Can enter without choosing any creatures")
    void canChooseNoCreatures() {
        castArmaggon(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ArmaggonFutureShark);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose zero targets even when creatures are available")
    void canLeaveAvailableCreaturesAlive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArmaggon(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player1, "Armaggon, Future Shark");
    }

    @Test
    @DisplayName("Can destroy two creatures controlled by different players")
    void canTargetCreaturesRegardlessOfController() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArmaggon(List.of(own.getId(), opposing.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unchosen);
        harness.assertOnBattlefield(player1, "Armaggon, Future Shark");
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's upkeep")
    void canBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArmaggon(List.of(target.getId()));

        harness.assertOnBattlefield(player1, "Armaggon, Future Shark");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The enters trigger destroys remaining targets when one leaves")
    void destroysRemainingLegalTarget() {
        Permanent departing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();
        harness.castCreature(player1, 0, List.of(departing.getId(), remaining.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Armaggon, Future Shark");
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(departing);
        gd.playerHands.get(player2.getId()).add(departing.getCard());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(departing.getCard());
    }

    @Test
    @DisplayName("Rejects selecting the same creature more than once")
    void rejectsDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects more than three target creatures")
    void rejectsFourTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    private void castArmaggon(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
