package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThaliaHereticCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's nonbasic lands enter tapped")
    void opponentsNonbasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player2, List.of(new FieldOfRuin()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Field of Ruin"))
                .findFirst().orElseThrow();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's basic lands do NOT enter tapped")
    void opponentsBasicLandsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null);

        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        assertThat(forest.isTapped()).isFalse();
    }
}
