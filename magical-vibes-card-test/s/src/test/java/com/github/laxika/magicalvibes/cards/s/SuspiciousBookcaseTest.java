package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspiciousBookcase.class, GreenwoodSentinel.class, Forest.class})
class SuspiciousBookcaseTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a target creature unblockable until end of turn")
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bookcase.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addCreatureReady(player1, new SuspiciousBookcase());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bookcase.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with fewer than three mana")
    void cannotActivateWithInsufficientMana() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bookcase.isTapped()).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent bookcase = harness.addToBattlefieldAndReturn(player1, new SuspiciousBookcase());
        bookcase.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bookcase.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped bookcase")
    void cannotActivateWhileTapped() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        bookcase.tap();
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        addActivationMana();

        harness.activateAbility(player1, 0, null, bookcase.getId());
        assertThat(bookcase.isTapped()).isTrue();
        assertThat(bookcase.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(bookcase.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after the bookcase leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent bookcase = addCreatureReady(player1, new SuspiciousBookcase());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bookcase);
        gd.playerGraveyards.get(player1.getId()).add(bookcase.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
