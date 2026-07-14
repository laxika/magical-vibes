package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AltarGolemTest extends BaseCardTest {

    // ===== Characteristic-defining P/T =====

    @Test
    @DisplayName("Power and toughness equal the number of creatures on the battlefield")
    void ptEqualsCreatureCount() {
        Permanent golem = addGolemReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Golem + two Grizzly Bears = 3 creatures.
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Altar Golem does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addGolemReady(player1);
        golem.tap();

        advanceToNextTurn(player2);

        assertThat(golem.isTapped()).isTrue();
    }

    // ===== Activated ability: tap five creatures to untap self =====

    @Test
    @DisplayName("Tapping five untapped creatures untaps Altar Golem")
    void tapFiveCreaturesUntapsGolem() {
        Permanent golem = addGolemReady(player1);
        golem.tap();
        // Exactly five untapped creatures — the cost auto-pays by tapping all of them.
        List<Permanent> fodder = addReadyCreatures(player1, 5);

        int golemIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        harness.activateAbility(player1, golemIdx, null, null);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isFalse();
        assertThat(fodder).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot activate with fewer than five untapped creatures")
    void cannotActivateWithFewerThanFive() {
        Permanent golem = addGolemReady(player1);
        golem.tap();
        // Only 4 untapped creatures available besides the tapped golem.
        addReadyCreatures(player1, 4);

        int golemIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        assertThatThrownBy(() -> harness.activateAbility(player1, golemIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addGolemReady(Player player) {
        Permanent perm = new Permanent(new AltarGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Permanent> addReadyCreatures(Player player, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Permanent perm = new Permanent(new GrizzlyBears());
                    perm.setSummoningSick(false);
                    gd.playerBattlefields.get(player.getId()).add(perm);
                    return perm;
                })
                .toList();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
