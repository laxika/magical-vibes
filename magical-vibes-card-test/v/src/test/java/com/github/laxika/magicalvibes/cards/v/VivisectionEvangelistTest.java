package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DuneMover;
import com.github.laxika.magicalvibes.cards.k.KothFireOfResistance;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivisectionEvangelist.class, DuneMover.class, KothFireOfResistance.class})
class VivisectionEvangelistTest extends BaseCardTest {

    @Test
    @DisplayName("Corrupted ETB destroys an opposing creature at three poison counters")
    void corruptedEtbDestroysOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dune Mover");
    }

    @Test
    @DisplayName("Corrupted ETB can destroy an opposing planeswalker")
    void corruptedEtbDestroysOpposingPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KothFireOfResistance());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    @DisplayName("Corrupted ETB does not trigger below three poison counters")
    void corruptedEtbDoesNotTriggerBelowThreePoisonCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castVivisectionEvangelist();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Corrupted ETB only targets a creature or planeswalker an opponent controls")
    void corruptedEtbRejectsOwnCreatureTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuneMover());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentCreature.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Corrupted does nothing if the opponent drops below three poison before resolution")
    void corruptedRechecksPoisonAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Dune Mover");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's poison counters do not enable corrupted")
    void controllersPoisonDoesNotEnableCorrupted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castVivisectionEvangelist();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Corrupted cannot target its source when the opponent has no legal permanent")
    void corruptedWithNoLegalTargets() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vivisection Evangelist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Increasing poison after entry cannot create a corrupted trigger retroactively")
    void corruptedDoesNotTriggerRetroactively() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castVivisectionEvangelist();
        harness.passBothPriorities();

        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("An opposing creature that changes control becomes an illegal target")
    void corruptedDoesNotDestroyTargetNowControlledByController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castVivisectionEvangelist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Dune Mover");
        assertThat(gd.stack).isEmpty();
    }

    private void castVivisectionEvangelist() {
        harness.setHand(player1, List.of(new VivisectionEvangelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
