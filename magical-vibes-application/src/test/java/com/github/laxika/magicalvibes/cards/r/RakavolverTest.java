package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Rakavolver.class)
class RakavolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Rakavolver enters without counters or granted abilities")
    void withoutKickers() {
        Permanent rakavolver = castRakavolver();

        assertThat(rakavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, rakavolver, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("White kicker adds two counters and the damage life-gain ability")
    void whiteKicker() {
        Permanent rakavolver = castWithWhiteKicker();

        assertThat(rakavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rakavolver, Keyword.FLYING)).isFalse();

        dealCombatDamageAndResolveTrigger(rakavolver);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Blue kicker adds one counter and flying")
    void blueKicker() {
        Permanent rakavolver = castWithBlueKicker();

        assertThat(rakavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rakavolver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Blue kicker does not grant the white kicker's life-gain ability")
    void blueKickerDoesNotGrantLifeGain() {
        Permanent rakavolver = castWithBlueKicker();

        dealCombatDamageAndResolveTrigger(rakavolver);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("White kicker gains life when Rakavolver deals combat damage to a creature")
    void whiteKickerGainsLifeFromDamageToCreature() {
        Permanent rakavolver = castWithWhiteKicker();
        Permanent blocker = addCreatureReady(player2, new Rakavolver());

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        rakavolver.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(rakavolver))));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Rakavolver");
    }

    @Test
    @DisplayName("Both kickers apply both sets of effects")
    void bothKickers() {
        Permanent rakavolver = castWithBothKickers();

        assertThat(rakavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, rakavolver, Keyword.FLYING)).isTrue();

        dealCombatDamageAndResolveTrigger(rakavolver);

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    private Permanent castRakavolver() {
        addMana(ManaColor.COLORLESS, 2);
        addMana(ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Rakavolver()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findRakavolver();
    }

    private Permanent castWithWhiteKicker() {
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.RED, 1);
        addMana(ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new Rakavolver()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of(), false);
        harness.passBothPriorities();
        return findRakavolver();
    }

    private Permanent castWithBlueKicker() {
        addMana(ManaColor.COLORLESS, 2);
        addMana(ManaColor.RED, 1);
        addMana(ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Rakavolver()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{U}"), false);
        harness.passBothPriorities();
        return findRakavolver();
    }

    private Permanent castWithBothKickers() {
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.RED, 1);
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Rakavolver()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{U}"), false);
        harness.passBothPriorities();
        return findRakavolver();
    }

    private void dealCombatDamageAndResolveTrigger(Permanent rakavolver) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        rakavolver.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }

    private Permanent findRakavolver() {
        return findPermanent(player1, "Rakavolver");
    }
}
