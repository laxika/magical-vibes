package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AangsDefense.class, Forest.class, GrizzlyBears.class})
class AangsDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your blocking creature and draws a card")
    void boostsBlockingCreatureAndDraws() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Aang's Defense");
    }

    @Test
    @DisplayName("Cannot target an opponent's blocking creature")
    void cannotTargetOpponentsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player2);
        setupDefense();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        setupDefense();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Does not resolve if the target stops blocking")
    void fizzlesIfTargetStopsBlocking() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    private void setupDefense() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AangsDefense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addBlockingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setBlocking(true);
        return creature;
    }
}
