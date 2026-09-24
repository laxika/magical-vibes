package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KingSolomonsFrogs.class, Forest.class, GrizzlyBears.class, HillGiant.class})
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
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Forest");
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
