package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidRetainer.class, CephalidLooter.class, AvenFisher.class, CephalidColiseum.class})
class CephalidRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature without flying")
    void tapsTargetCreatureWithoutFlying() {
        addCreatureReady(player1, new CephalidRetainer());
        Permanent target = addCreatureReady(player2, new CephalidLooter());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new CephalidRetainer());
        Permanent target = addCreatureReady(player2, new AvenFisher());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new CephalidRetainer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CephalidColiseum());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("Can target a creature that is already tapped")
    void canTargetAlreadyTappedCreature() {
        Permanent retainer = addCreatureReady(player1, new CephalidRetainer());
        Permanent target = addCreatureReady(player2, new CephalidLooter());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(retainer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can tap itself")
    void canTapItself() {
        Permanent retainer = addCreatureReady(player1, new CephalidRetainer());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, retainer.getId());
        harness.passBothPriorities();

        assertThat(retainer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent retainer = harness.addToBattlefieldAndReturn(player1, new CephalidRetainer());
        retainer.setSummoningSick(true);
        retainer.tap();
        Permanent firstTarget = addCreatureReady(player2, new CephalidLooter());
        Permanent secondTarget = addCreatureReady(player2, new CephalidLooter());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.activateAbility(player1, 0, null, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(retainer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot substitute colorless mana for the second blue mana")
    void requiresTwoBlueMana() {
        addCreatureReady(player1, new CephalidRetainer());
        Permanent target = addCreatureReady(player2, new CephalidLooter());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
