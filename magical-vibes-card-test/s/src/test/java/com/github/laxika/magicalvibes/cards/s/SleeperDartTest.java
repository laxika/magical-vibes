package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({SleeperDart.class, AlmightyBrushwagg.class, Forest.class})
class SleeperDartTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersDrawsCard() {
        harness.setHand(player1, List.of(new SleeperDart()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the dart and skips the target creature's next untap")
    void abilitySacrificesAndSkipsNextUntap() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sleeper Dart");
        harness.assertInGraveyard(player1, "Sleeper Dart");

        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();

        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves")
    void sacrificeIsPaidImmediately() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Sleeper Dart");
        harness.assertInGraveyard(player1, "Sleeper Dart");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped target is not tapped and the restriction expires on its next untap step")
    void untappedTargetConsumesRestrictionWithoutBeingTapped() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.isTapped()).isFalse();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();

        target.tap();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A friendly creature is legal and another player's untap step does not consume the restriction")
    void friendlyCreatureSkipsOnlyItsControllersNextUntap() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent target = addCreatureReady(player1, new AlmightyBrushwagg());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped dart cannot pay the tap cost and is not sacrificed")
    void tappedDartCannotActivate() {
        Permanent dart = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        dart.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sleeper Dart");
        assertThat(gd.stack).isEmpty();
    }

}
