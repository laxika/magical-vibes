package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.ObsidianGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeldwebStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an oil counter")
    void entersWithOilCounter() {
        harness.setHand(player1, List.of(new MeldwebStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent strider = findPermanent(player1, "Meldweb Strider");
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("Removing an oil counter animates it until end of turn")
    void removingOilCounterAnimatesIt() {
        Permanent strider = addReadyStrider(1);

        harness.activateAbility(player1, indexOf(strider), 0, null, null);
        harness.passBothPriorities();

        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.isCreature(gd, strider)).isTrue();
        assertThat(gqs.isArtifact(strider)).isTrue();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("Cannot remove an oil counter when it has none")
    void cannotRemoveMissingOilCounter() {
        Permanent strider = addReadyStrider(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(strider), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates it and taps the crewing creature")
    void crewAnimatesIt() {
        Permanent strider = addReadyStrider(0);
        Permanent giant = addReadyCreature(player1, new ObsidianGiant());

        harness.activateAbility(player1, indexOf(strider), 1, null, null);
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, strider)).isTrue();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);
    }

    private Permanent addReadyStrider(int oilCounters) {
        Permanent strider = new Permanent(new MeldwebStrider());
        strider.setSummoningSick(false);
        strider.setCounterCount(CounterType.OIL, oilCounters);
        gd.playerBattlefields.get(player1.getId()).add(strider);
        return strider;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
