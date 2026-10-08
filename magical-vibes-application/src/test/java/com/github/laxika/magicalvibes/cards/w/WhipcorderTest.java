package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whipcorder.class, GlorySeeker.class, Forest.class})
class WhipcorderTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        Permanent whipcorder = addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(whipcorder.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new Whipcorder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new Whipcorder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent whipcorder = findPermanent(player1, "Whipcorder");
        assertThat(whipcorder.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(whipcorder));
        harness.passBothPriorities();

        assertThat(whipcorder.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent whipcorder = addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(whipcorder.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent whipcorder = addCreatureReady(player1, new Whipcorder());
        whipcorder.tap();
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent whipcorder = addCreatureReady(player1, new Whipcorder());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, whipcorder.getId());
        assertThat(whipcorder.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(whipcorder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target a creature that is already tapped")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after Whipcorder leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent whipcorder = addCreatureReady(player1, new Whipcorder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(whipcorder);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Face-down Whipcorder has no printed tap ability")
    void cannotActivatePrintedAbilityWhileFaceDown() {
        Permanent whipcorder = castFaceDownWhipcorder();
        whipcorder.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        assertThat(whipcorder.isFaceDown()).isTrue();
        assertThat(whipcorder.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Turning face up requires white mana")
    void cannotTurnFaceUpWithColorlessMana() {
        Permanent whipcorder = castFaceDownWhipcorder();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(whipcorder.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Turning face up is immediate and restores the tap ability")
    void turningFaceUpRestoresAbilityImmediately() {
        Permanent whipcorder = castFaceDownWhipcorder();
        whipcorder.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(whipcorder.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(whipcorder.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Turning face up does not remove summoning sickness")
    void turningFaceUpDoesNotRemoveSummoningSickness() {
        Permanent whipcorder = castFaceDownWhipcorder();
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(whipcorder.isFaceDown()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(whipcorder.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent castFaceDownWhipcorder() {
        harness.setHand(player1, List.of(new Whipcorder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Whipcorder");
    }
}
