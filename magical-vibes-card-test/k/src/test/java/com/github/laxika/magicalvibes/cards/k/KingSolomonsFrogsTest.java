package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KingSolomonsFrogs.class, Forest.class, GrizzlyBears.class, HillGiant.class, Commandeer.class})
class KingSolomonsFrogsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a qualifying permanent and its controller draws a card")
    void exilesQualifyingPermanentAndControllerDraws() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of(new Forest()));

        castFrogs(List.of(giant.getId()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Hill Giant");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsControlledBySameOpponent() {
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new KingSolomonsFrogs()));
        addFrogsMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstGiant.getId(), secondGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Requires an opponent permanent with mana value at least three")
    void requiresQualifyingOpponentPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KingSolomonsFrogs()));
        addFrogsMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }

    @Test
    @DisplayName("Exiling the frogs makes its controller the monarch")
    void exilingFrogsMakesControllerMonarch() {
        harness.addToBattlefield(player1, new KingSolomonsFrogs());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "King Solomon's Frogs");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("King Solomon's Frogs");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exile is paid immediately while becoming monarch waits for resolution")
    void exileIsAnActivationCost() {
        harness.addToBattlefield(player1, new KingSolomonsFrogs());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "King Solomon's Frogs");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("King Solomon's Frogs");
        assertThat(gd.monarchPlayerId).isNull();
        harness.passBothPriorities();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("May decline exile even when an opponent has a qualifying permanent")
    void mayChooseNoTargets() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        castFrogs(List.of());

        harness.assertOnBattlefield(player1, "King Solomon's Frogs");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger exile")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new KingSolomonsFrogs());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile a qualifying permanent controlled by its own controller")
    void cannotTargetOwnPermanent() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new KingSolomonsFrogs()));
        addFrogsMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A player who steals the spell did not cast it and gets no exile trigger")
    void stolenSpellDoesNotTriggerForNewController() {
        KingSolomonsFrogs frogs = new KingSolomonsFrogs();
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(frogs));
        addFrogsMana();
        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, frogs.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "King Solomon's Frogs");
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castFrogs(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new KingSolomonsFrogs()));
        addFrogsMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addFrogsMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
