package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InnocuousInsect.class, GrizzlyBears.class, Counterspell.class})
class InnocuousInsectTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Innocuous Insect draws a card before it enters the battlefield")
    void castingDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InnocuousInsect()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Innocuous Insect");
    }

    @Test
    @DisplayName("Paying buyback returns Innocuous Insect to its owner's hand")
    void buybackReturnsToHand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        InnocuousInsect insect = new InnocuousInsect();
        harness.setHand(player1, List.of(insect));
        addMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Innocuous Insect");
        harness.assertNotInGraveyard(player1, "Innocuous Insect");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Countering the creature does not counter its draw trigger or apply buyback")
    void counteredSpellStillDrawsCard(boolean buyback) {
        InnocuousInsect insect = new InnocuousInsect();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(insect));
        harness.setHand(player2, List.of(new Counterspell()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        if (buyback) {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castInstantWithBuyback(player1, 0, null);
        } else {
            harness.castCreature(player1, 0);
        }

        harness.castAndResolveInstant(player2, 0, insect.getId());

        harness.assertInGraveyard(player1, "Innocuous Insect");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertNotOnBattlefield(player1, "Innocuous Insect");
        harness.assertInGraveyard(player1, "Innocuous Insect");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep and still draws a card")
    void castsDuringOpponentsUpkeep() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new InnocuousInsect()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertOnBattlefield(player1, "Innocuous Insect");
    }

    @Test
    @DisplayName("Entering without being cast does not trigger a draw")
    void enteringWithoutCastingDoesNotDraw() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.enterBattlefieldAndReturn(player1, new InnocuousInsect());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertOnBattlefield(player1, "Innocuous Insect");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Innocuous Insect")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new InnocuousInsect());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
