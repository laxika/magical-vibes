package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RalZarekTest extends BaseCardTest {

    @Test
    @DisplayName("+1 taps the first target and untaps the second")
    void plusOneTapsThenUntaps() {
        Permanent ral = addReadyRal(player1, 4);
        Permanent toTap = addPermanent(player2, new GrizzlyBears());
        Permanent toUntap = addPermanent(player1, new Mountain());
        toUntap.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(toTap.getId(), toUntap.getId()));
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(toTap.isTapped()).isTrue();
        assertThat(toUntap.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 cannot choose the same permanent for both targets")
    void plusOneRequiresTwoDifferentPermanents() {
        addReadyRal(player1, 4);
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 cannot be activated with only one target")
    void plusOneRequiresBothTargets() {
        addReadyRal(player1, 4);
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-2 deals 3 damage to a target creature")
    void minusTwoDamagesCreature() {
        Permanent ral = addReadyRal(player1, 4);
        Permanent bears = addPermanent(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 deals 3 damage to a target player")
    void minusTwoDamagesPlayer() {
        addReadyRal(player1, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("-7 grants one extra turn per coin that came up heads")
    void minusSevenGrantsExtraTurnPerHeads() {
        Permanent ral = addReadyRal(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();

        String flipLog = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(text -> text.contains("flips 5 coins for Ral Zarek"))
                .findFirst()
                .orElseThrow();
        int heads = Integer.parseInt(flipLog.replaceAll(".*: (\\d+) heads\\.", "$1"));

        assertThat(heads).isBetween(0, 5);
        assertThat(gd.extraTurns).hasSize(heads);
        assertThat(gd.extraTurns).allMatch(id -> id.equals(player1.getId()));
    }

    private Permanent addReadyRal(Player player, int loyalty) {
        Permanent perm = new Permanent(new RalZarek());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
