package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimeAndTideTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out every creature with phasing")
    void phasesOutCreaturesWithPhasing() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());
        Permanent opponentKeeper = addCreatureReady(player2, new Breezekeeper());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentKeeper);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentKeeper);
    }

    @Test
    @DisplayName("Phases in every phased-out creature")
    void phasesInPhasedOutCreatures() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        phaseOutWithRealityRipple(bears.getId());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Simultaneously phases in phased-out creatures and phases out creatures with phasing")
    void simultaneousSwap() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        phaseOutWithRealityRipple(bears.getId());
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);
    }

    @Test
    @DisplayName("A phased-out creature with phasing phases in and does not immediately phase out")
    void phasedOutPhasingCreaturePhasesInAndStays() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());
        advanceToControllersUntap(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(keeper);
    }

    @Test
    @DisplayName("Does not phase out creatures without phasing")
    void ignoresCreaturesWithoutPhasing() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Does not phase in phased-out noncreature permanents")
    void ignoresPhasedOutNoncreatures() {
        Permanent island = new Permanent(new Island());
        gd.phasedOutPermanents.computeIfAbsent(player1.getId(), id -> new ArrayList<>()).add(island);

        castTimeAndTide();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);
    }

    private void castTimeAndTide() {
        harness.setHand(player1, List.of(new TimeAndTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void phaseOutWithRealityRipple(UUID targetId) {
        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void advanceToControllersUntap(Player controller) {
        if (gd.activePlayerId.equals(controller.getId())) {
            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
        } else {
            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
        }
    }
}
