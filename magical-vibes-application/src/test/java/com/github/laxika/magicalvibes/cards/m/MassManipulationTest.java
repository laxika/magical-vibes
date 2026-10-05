package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MassManipulation.class, AxebaneBeast.class, DovinGrandArbiter.class, AzoriusGuildgate.class})
class MassManipulationTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of exactly X target creatures and planeswalkers")
    void gainsPermanentControlOfExactlyXTargets() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        Permanent planeswalker = addReadyPlaneswalker(player2);

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8); // X=2: {2}{2}{U}{U}{U}{U}
        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(creature.getId(), planeswalker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(creature.getId(), planeswalker.getId());
    }

    @Test
    @DisplayName("X=0 resolves with no targets")
    void xZeroDoesNothing() {
        addCreatureReady(player2, new AxebaneBeast());

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Axebane Beast");
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AzoriusGuildgate());

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=1: {1}{1}{U}{U}{U}{U}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Targets must be creatures and/or planeswalkers");
    }

    @Test
    @DisplayName("Remaining legal targets are controlled when another target leaves the battlefield")
    void gainsControlOfRemainingLegalTarget() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        Permanent planeswalker = addReadyPlaneswalker(player2);
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), planeswalker.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(planeswalker.getId()).doesNotContain(creature.getId());
        harness.assertInGraveyard(player2, "Axebane Beast");
        harness.assertInGraveyard(player1, "Mass Manipulation");
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void rejectsDuplicateTargets() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Both X symbols must be paid")
    void requiresManaForBothXSymbols() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Axebane Beast");
    }

    @Test
    @DisplayName("Can target your own creature without untapping it or making it summoning sick")
    void canTargetOwnCreature() {
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Axebane Beast");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isSummoningSick()).isFalse();
        harness.assertInGraveyard(player1, "Mass Manipulation");
    }

    @Test
    @DisplayName("Control lasts beyond cleanup and the spell entering the graveyard")
    void controlPersistsIntoNextTurn() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isSummoningSick()).isTrue();
        harness.assertInGraveyard(player1, "Mass Manipulation");
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Does not resolve when every target leaves the battlefield")
    void allTargetsGone() {
        Permanent creature = addCreatureReady(player2, new AxebaneBeast());
        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 1, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Axebane Beast");
        harness.assertInGraveyard(player1, "Mass Manipulation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).doesNotContain(creature.getId());
    }

    private Permanent addReadyPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }
}
