package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chronostutter.class, RuneclawBear.class, Island.class})
class ChronostutterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target creature second from the top of its owner's library")
    void putsTargetCreatureSecondFromTopOfOwnersLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island(), new Island()));

        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1)).isSameAs(bears.getCard());
        harness.assertInGraveyard(player1, "Chronostutter");
    }

    @Test
    void putsCreatureOnTopOfEmptyLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bear.getCard());
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void putsOwnCreatureBelowOnlyCardInLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bear.getCard());
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void usesOwnersLibraryForCreatureControlledByOpponent() {
        Card creature = new RuneclawBear();
        creature.setOwnerId(player2.getId());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, creature);
        Card ownersTop = new Island();
        Card controllersTop = new Island();
        harness.setLibrary(player2, List.of(ownersTop));
        harness.setLibrary(player1, List.of(controllersTop));
        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownersTop, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllersTop);
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void doesNotMoveCreatureThatLeftBattlefieldBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Card top = new Island();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(new Chronostutter()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0, bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Chronostutter");
        assertThat(gd.stack).isEmpty();
    }
}
