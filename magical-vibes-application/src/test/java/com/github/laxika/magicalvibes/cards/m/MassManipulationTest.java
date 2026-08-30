package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MassManipulationTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of exactly X target creatures and planeswalkers")
    void gainsPermanentControlOfExactlyXTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addReadyPlaneswalker(player2);

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8); // X=2: {2}{2}{U}{U}{U}{U}
        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(creature.getId(), planeswalker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(creature.getId(), planeswalker.getId());
    }

    @Test
    @DisplayName("X=0 resolves with no targets")
    void xZeroDoesNothing() {
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new MassManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=1: {1}{1}{U}{U}{U}{U}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    private Permanent addReadyPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
