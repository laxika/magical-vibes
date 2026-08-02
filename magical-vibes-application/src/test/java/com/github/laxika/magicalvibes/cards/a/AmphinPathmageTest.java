package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmphinPathmageTest extends BaseCardTest {

    @Test
    @DisplayName("Makes target creature unblockable until end of turn")
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent pathmage = addReadyPathmage(player1);
        Permanent target = addCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(pathmage.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addReadyPathmage(player1);
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

    private Permanent addReadyPathmage(Player player) {
        Permanent pathmage = new Permanent(new AmphinPathmage());
        pathmage.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(pathmage);
        return pathmage;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
