package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostveilAmbush.class, AlmightyBrushwagg.class, Forest.class})
class FrostveilAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target only one creature")
    void canTargetOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards Frostveil Ambush and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Frostveil Ambush");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can resolve with no targets without affecting creatures")
    void canChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Frostveil Ambush");
    }

    @Test
    @DisplayName("An already tapped creature skips only its controller's next untap")
    void alreadyTappedCreatureSkipsOneUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creatures controlled by different players each skip their own next untap")
    void targetsCreaturesOfEitherController() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(opposing.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still affects a remaining legal target when the other leaves the battlefield")
    void oneTargetLeavesBeforeResolution() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Frostveil Ambush");
    }

    @Test
    @DisplayName("Cycling pays the discard cost before the draw ability resolves")
    void cyclingDiscardsImmediately() {
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Frostveil Ambush");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Almighty Brushwagg");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two Ambushes before the same untap do not skip two untap steps")
    void repeatedAmbushesExpireDuringSameUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush(), new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not affect other creatures when all chosen targets leave")
    void allTargetsLeaveBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FrostveilAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.passBothPriorities();

        assertThat(unchosen.isTapped()).isFalse();
        assertThat(unchosen.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Frostveil Ambush");
    }
}
