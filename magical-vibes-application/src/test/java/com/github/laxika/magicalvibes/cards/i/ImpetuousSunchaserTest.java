package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({ImpetuousSunchaser.class, NyxbornRollicker.class})
class ImpetuousSunchaserTest extends BaseCardTest {

    @Test
    @DisplayName("Impetuous Sunchaser must attack each combat if able")
    void mustAttackWhenAble() {
        addSunchaser(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Impetuous Sunchaser can attack while summoning sick because it has haste")
    void hasteAllowsItToAttackImmediately() {
        addSunchaser(true);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Impetuous Sunchaser while declaring another attacker is rejected")
    void mustBeIncludedAmongAttackers() {
        addSunchaser(false);

        addCreatureReady(player1, new NyxbornRollicker());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void addSunchaser(boolean summoningSick) {
        Permanent sunchaser = harness.addToBattlefieldAndReturn(player1, new ImpetuousSunchaser());
        sunchaser.setSummoningSick(summoningSick);
    }

    @Test
    @DisplayName("A newly entered Sunchaser can be declared as an attacker")
    void newlyEnteredSunchaserAttacks() {
        addSunchaser(true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Sunchaser is not required to attack")
    void tappedSunchaserMayBeOmitted() {
        Permanent sunchaser = addCreatureReady(player1, new ImpetuousSunchaser());
        sunchaser.tap();
        addCreatureReady(player1, new NyxbornRollicker());

        assertThatCode(() -> declareAttackersAndPrepareBlockers(List.of(1)))
                .doesNotThrowAnyException();
        assertThat(sunchaser.isAttacking()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A ground creature cannot block Sunchaser")
    void flyingPreventsGroundBlocker() {
        addSunchaser(false);
        addCreatureReady(player2, new NyxbornRollicker());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature can block Sunchaser")
    void flyingCreatureCanBlock() {
        addSunchaser(false);
        Permanent blocker = addCreatureReady(player2, new ImpetuousSunchaser());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }
}
