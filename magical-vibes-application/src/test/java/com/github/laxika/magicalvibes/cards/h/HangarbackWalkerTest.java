package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HangarbackWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 enters with two +1/+1 counters and is a 2/2")
    void entersWithXPlusOneCounters() {
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent walker = findWalker(player1);
        assertThat(walker).isNotNull();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {1}, {T} puts a +1/+1 counter on it and taps it")
    void activatedAbilityAddsCounter() {
        Permanent walker = addWalkerReady(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dying creates one 1/1 flying Thopter per +1/+1 counter")
    void deathCreatesThopterPerCounter() {
        addWalkerReady(player1, 3);

        killWithWrath();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(3);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Only +1/+1 counters are counted for the Thopters")
    void otherCounterTypesDoNotMakeThopters() {
        Permanent walker = addWalkerReady(player1, 1);
        walker.setCounterCount(CounterType.CHARGE, 2);

        killWithWrath();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    private void killWithWrath() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        gs.playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addWalkerReady(Player player, int counters) {
        Permanent perm = new Permanent(new HangarbackWalker());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findWalker(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hangarback Walker"))
                .findFirst().orElse(null);
    }
}
