package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelethopterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped creature you control gives Telethopter flying")
    void tappingCreatureGrantsFlying() {
        Permanent thopter = addReady(player1, new Telethopter());
        thopter.tap();
        Permanent fodder = addReady(player1, new GrizzlyBears());

        activate(thopter);

        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(fodder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent thopter = addReady(player1, new Telethopter());
        thopter.tap();
        addReady(player1, new GrizzlyBears());

        activate(thopter);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();

        advanceToNextTurn();

        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent thopter = addReady(player1, new Telethopter());
        thopter.tap();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent thopter) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
