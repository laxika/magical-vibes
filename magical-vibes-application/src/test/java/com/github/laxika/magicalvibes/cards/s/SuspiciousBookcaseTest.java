package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuspiciousBookcaseTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a target creature unblockable until end of turn")
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent bookcase = addReadyBookcase(player1);
        Permanent target = addCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bookcase.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addReadyBookcase(player1);
        Permanent target = addCreature(player1);
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
        Permanent bookcase = addReadyBookcase(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bookcase.isTapped()).isFalse();
    }

    private Permanent addReadyBookcase(Player player) {
        Permanent bookcase = new Permanent(new SuspiciousBookcase());
        bookcase.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bookcase);
        return bookcase;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
