package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlhammarretsArchive.class, BardKingOfDale.class, BladeSplicer.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class})
class BardKingOfDaleTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles draws except the first draw in each of the controller's draw steps")
    void doublesExtraDrawsButNotFirstDrawStepDraw() {
        harness.addToBattlefield(player1, new BardKingOfDale());
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
    @DisplayName("Doubles a draw outside the controller's draw step")
    void doublesDrawOutsideDrawStep() {
        harness.addToBattlefield(player1, new BardKingOfDale());
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
    @DisplayName("Doubles tokens created under the controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new BardKingOfDale());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    @DisplayName("Does not affect an opponent's draws")
    void doesNotAffectOpponent() {
        harness.addToBattlefield(player1, new BardKingOfDale());

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
    @DisplayName("Does not affect tokens created under an opponent's control")
    void doesNotAffectOpponentTokens() {
        harness.addToBattlefield(player1, new BardKingOfDale());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    @DisplayName("Doubles the first draw during an opponent's draw step")
    void doublesDrawDuringOpponentDrawStep() {
        harness.addToBattlefield(player1, new BardKingOfDale());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A two-card instruction during the controller's draw step draws three cards")
    void exemptsOnlyFirstCardOfMultiCardDraw() {
        harness.addToBattlefield(player1, new BardKingOfDale());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({AlhammarretsArchive.class, BardKingOfDale.class, Forest.class, Island.class})
    @DisplayName("Bard and Alhammarret's Archive multiply an eligible draw to four cards")
    void stacksWithAnotherDrawDoubler() {
        harness.addToBattlefield(player1, new BardKingOfDale());
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Island(), new Forest(), new Island(), new Forest()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
