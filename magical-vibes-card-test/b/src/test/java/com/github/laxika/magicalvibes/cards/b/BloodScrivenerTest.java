package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.PilferedPlans;
import com.github.laxika.magicalvibes.cards.t.TurnBurn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodScrivener.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class,
        PilferedPlans.class, NotionThief.class, TurnBurn.class})
class BloodScrivenerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing with an empty hand draws two cards and loses 1 life")
    void emptyHandDrawDrawsTwoAndLosesLife() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Drawing with cards in hand is a normal single draw with no life loss")
    void nonEmptyHandDrawIsUnchanged() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new Peek(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("An opponent's empty-hand draw is not replaced")
    void doesNotAffectOpponentDraw() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.setLibrary(player2, List.of(
                new Forest(),
                new GrizzlyBears()
        ));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Two Scriveners replace one empty-hand draw with three cards and two life lost")
    void multipleScrivenersEachApply() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.setLibrary(player1, List.of(new BloodScrivener(), new BloodScrivener(),
                new BloodScrivener(), new BloodScrivener()));
        harness.setHand(player1, List.of(new Peek()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only the first draw of a two-card instruction is replaced")
    void multiCardDrawAddsOnlyOneCard() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.setLibrary(player1, List.of(new BloodScrivener(), new BloodScrivener(),
                new BloodScrivener(), new BloodScrivener()));
        harness.setLibrary(player2, List.of(new BloodScrivener(), new BloodScrivener()));
        harness.setHand(player1, List.of(new PilferedPlans()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A draw redirected by Notion Thief is replaced for its empty-handed controller")
    void appliesToDrawCreatedByAnotherReplacement() {
        harness.addToBattlefield(player1, new BloodScrivener());
        harness.addToBattlefield(player1, new NotionThief());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BloodScrivener(), new BloodScrivener(),
                new BloodScrivener()));
        harness.setHand(player2, List.of(new Peek(), new Island()));
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A Scrivener that has lost its abilities does not replace draws")
    void abilityRemovalDisablesReplacement() {
        var scrivener = harness.addToBattlefieldAndReturn(player1, new BloodScrivener());
        harness.setHand(player2, List.of(new TurnBurn()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, 0, scrivener.getId());
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new BloodScrivener(), new BloodScrivener()));
        harness.setHand(player1, List.of(new Peek()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }
}
