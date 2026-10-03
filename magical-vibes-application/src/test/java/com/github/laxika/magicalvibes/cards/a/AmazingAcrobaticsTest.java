package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmazingAcrobatics.class, GrizzlyBears.class, Spellbook.class})
class AmazingAcrobaticsTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode counters the target spell")
    void counterModeCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AmazingAcrobatics()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0},
                bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tap mode taps one or two target creatures")
    void tapModeTapsTwoTargetCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castCard(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both modes counter a spell and tap a creature")
    void bothModesResolveWithSeparateTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new AmazingAcrobatics()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0, 1},
                bears.getId(), List.of(other.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        castCard(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tap mode can target a single creature you control")
    void tapModeTapsOneOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCard(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Amazing Acrobatics");
    }

    @Test
    @DisplayName("Tap mode requires at least one creature target")
    void tapModeRejectsZeroTargets() {
        castCard(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap mode cannot target more than two creatures")
    void tapModeRejectsThreeTargets() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        castCard(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap mode cannot select the same creature twice")
    void tapModeRejectsDuplicateTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castCard(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap mode still taps the remaining legal creature target")
    void tapModeResolvesWhenOneTargetLeavesBattlefield() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        castCard(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Amazing Acrobatics");
    }

    @Test
    @DisplayName("Both modes counter a spell and tap two creatures with different controllers")
    void bothModesCounterAndTapTwoCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent own = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player1, new GrizzlyBears());
        castCard(player2);

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0, 1}, bears.getId(),
                List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both modes still tap a creature after the spell target is countered in response")
    void bothModesTapWhenSpellTargetLeavesStack() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new AmazingAcrobatics()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCard(player2);

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0, 1}, bears.getId(),
                List.of(creature.getId()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Amazing Acrobatics");
    }

    @Test
    @DisplayName("Both modes still counter the spell after the creature target leaves the battlefield")
    void bothModesCounterWhenCreatureTargetLeavesBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCard(player2);

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0, 1}, bears.getId(),
                List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Amazing Acrobatics");
    }

    private void castCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new AmazingAcrobatics()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
