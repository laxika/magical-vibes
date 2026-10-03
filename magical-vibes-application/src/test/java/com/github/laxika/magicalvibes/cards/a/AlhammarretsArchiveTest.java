package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.cards.u.UbaMask;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlhammarretsArchive.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class,
        ArtificersEpiphany.class, ThoughtReflection.class, UbaMask.class})
class AlhammarretsArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("A draw outside the controller's draw step draws two cards instead")
    void doublesNonDrawStepDraw() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
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
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island(),
                new Forest()
        ));
        harness.forceStep(TurnStep.DRAW);
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A draw during the opponent's draw step is doubled for the controller")
    void doublesDrawInOpponentsDrawStep() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
        harness.forceStep(TurnStep.DRAW);
        harness.forceActivePlayer(player2);

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

        harness.setLibrary(player2, List.of(
                new Forest(),
                new GrizzlyBears()
        ));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void doublesEachCardOfMultiCardDraw() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island(), new Forest()));
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawDoublersApplyCumulatively() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island(), new Forest()));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void firstDrawStepDrawDoubledByReflectionDrawsThreeWithArchive() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void replacedDrawDoesNotConsumeFirstActualDrawStepDrawExemption() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.addToBattlefield(player2, new UbaMask());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.inMutationScope(() -> gd.playerBattlefields.get(player2.getId()).clear());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doublesLifeGainedWhenLifeTotalIsSetHigher() {
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setLife(player1, 3);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 10));

        harness.assertLife(player1, 17);
    }
}
