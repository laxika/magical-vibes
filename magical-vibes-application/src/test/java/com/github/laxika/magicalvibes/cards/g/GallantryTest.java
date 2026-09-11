package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gallantry.class, CanopySpider.class, Forest.class})
class GallantryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +4/+4 and draws a card")
    void boostsBlockingCreatureAndDraws() {
        Permanent blocker = addBlockingCreature(player1);
        setupGallantry();
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(4);
        assertThat(blocker.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gallantry");
    }

    @Test
    @DisplayName("Can target an opponent's blocking creature")
    void boostsOpponentsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player2);
        setupGallantry();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(4);
        assertThat(blocker.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent blocker = addBlockingCreature(player1);
        setupGallantry();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addBlockingCreature(player1);
        Permanent bystander = addCreatureReady(player1, new CanopySpider());
        setupGallantry();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent that is blocking")
    void cannotTargetNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Forest());
        noncreature.setBlocking(true);
        setupGallantry();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Does nothing if the target stops blocking before resolution")
    void fizzlesIfTargetStopsBlocking() {
        Permanent blocker = addBlockingCreature(player1);
        setupGallantry();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setupGallantry() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Gallantry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new CanopySpider());
        creature.setBlocking(true);
        return creature;
    }
}
