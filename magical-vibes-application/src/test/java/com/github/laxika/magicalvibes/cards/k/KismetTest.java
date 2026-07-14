package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KismetTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new Kismet());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = permanentOf(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's artifacts enter tapped")
    void opponentsArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new Kismet());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = permanentOf(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands enter tapped")
    void opponentsLandsEnterTapped() {
        harness.addToBattlefield(player1, new Kismet());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null);

        Permanent forest = permanentOf(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's permanents do NOT enter tapped")
    void controllersPermanentsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new Kismet());
        harness.setHand(player1, List.of(new Forest()));

        gs.playCard(gd, player1, 0, 0, null, null);

        Permanent forest = permanentOf(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent permanentOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
