package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlhammarretsArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("A draw outside the controller's draw step draws two cards instead")
    void doublesNonDrawStepDraw() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The first draw in the controller's own draw step is not doubled, later ones are")
    void exemptsFirstDrawStepDraw() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island(),
                new Forest()
        )));
        harness.forceStep(TurnStep.DRAW);
        gd.activePlayerId = player1.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A draw during the opponent's draw step is doubled for the controller")
    void doublesDrawInOpponentsDrawStep() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.forceStep(TurnStep.DRAW);
        gd.activePlayerId = player2.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller gains twice as much life")
    void doublesLifeGain() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("An opponent's life gain and draws are unaffected")
    void doesNotAffectOpponent() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.assertLife(player2, 23);

        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
