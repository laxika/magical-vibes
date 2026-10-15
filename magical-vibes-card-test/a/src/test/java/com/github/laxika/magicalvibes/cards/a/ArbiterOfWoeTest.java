package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArbiterOfWoe.class, BearCub.class, Forest.class})
class ArbiterOfWoeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Arbiter of Woe requires sacrificing a creature")
    void castingRequiresSacrificingCreature() {
        harness.setHand(player1, List.of(new ArbiterOfWoe()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Arbiter of Woe's enters-the-battlefield ability has its full effect")
    void entersTheBattlefieldAbility() {
        Permanent sacrifice = addCreatureReady(player1, new BearCub());
        harness.setHand(player2, List.of(new BearCub()));
        harness.setLibrary(player1, List.of(new Forest()));

        castArbiter(sacrifice);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Bear Cub");
        harness.assertInGraveyard(player1, "Bear Cub");
    }

    @Test
    @DisplayName("The sacrifice cost is paid before Arbiter of Woe resolves")
    void sacrificeIsPaidAtCastingTime() {
        Permanent sacrifice = addCreatureReady(player1, new BearCub());

        castArbiter(sacrifice);

        harness.assertInGraveyard(player1, "Bear Cub");
        harness.assertNotOnBattlefield(player1, "Bear Cub");
        harness.assertNotOnBattlefield(player1, "Arbiter of Woe");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An empty opposing hand does not stop the rest of the enter ability")
    void emptyOpposingHandStillDrainsAndDraws() {
        Permanent sacrifice = addCreatureReady(player1, new BearCub());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        castArbiter(sacrifice);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Arbiter of Woe");
    }

    @Test
    @DisplayName("The opponent chooses exactly one card to discard")
    void opponentChoosesDiscard() {
        Permanent sacrifice = addCreatureReady(player1, new BearCub());
        harness.setHand(player2, List.of(new Forest(), new BearCub()));
        harness.setLibrary(player1, List.of(new Forest()));

        castArbiter(sacrifice);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player2, "Bear Cub");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = addCreatureReady(player2, new BearCub());

        assertThatThrownBy(() -> castArbiter(sacrifice))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("A noncreature cannot pay the sacrifice cost")
    void cannotSacrificeNoncreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> castArbiter(sacrifice))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    private void castArbiter(Permanent sacrifice) {
        harness.setHand(player1, List.of(new ArbiterOfWoe()));
        addMana();
        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
