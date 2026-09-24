package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleverConcealment.class, Forest.class, GrizzlyBears.class, Millstone.class})
class CleverConcealmentTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out any number of selected nonland permanents you control")
    void phasesOutSelectedNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(creature.getId(), artifact.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Allows choosing no permanents")
    void allowsChoosingNoPermanents() {
        castAndResolve(List.of());

        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by an opponent")
    void cannotTargetIllegalPermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent you control");

        prepareCast();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent you control");
    }

    @Test
    @DisplayName("Phased-out permanents phase in during their controller's next untap")
    void phasesBackInDuringNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(List.of(creature.getId()));
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void castAndResolve(List<UUID> targetIds) {
        prepareCast();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
