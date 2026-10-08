package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({WarhornBlast.class, FearlessPup.class})
class WarhornBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts creatures you control by +2/+1")
    void resolvingBoostsOwnCreatures() {
        Permanent ownPup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        Permanent opponentPup = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownPup.getPowerModifier()).isEqualTo(2);
        assertThat(ownPup.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentPup.getPowerModifier()).isZero();
        assertThat(opponentPup.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost ends at cleanup")
    void boostEndsAtCleanup() {
        Permanent ownPup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownPup.getPowerModifier()).isZero();
        assertThat(ownPup.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can be foretold and cast for {2}{W} on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        Permanent ownPup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        WarhornBlast blast = new WarhornBlast();
        harness.setHand(player1, List.of(blast));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(blast.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, blast.getId());
        harness.passBothPriorities();

        assertThat(ownPup.getPowerModifier()).isEqualTo(2);
        assertThat(ownPup.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only creatures present when the spell resolves receive the boost")
    void boostsCreaturesPresentAtResolutionOnly() {
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new FearlessPup());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new FearlessPup());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(2);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Resolves with no creatures under your control")
    void resolvesWithNoOwnCreatures() {
        Permanent opponentPup = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Warhorn Blast");
        assertThat(opponentPup.getPowerModifier()).isZero();
        assertThat(opponentPup.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot cast the card on the turn it was foretold")
    void cannotCastOnForetellTurn() {
        WarhornBlast blast = new WarhornBlast();
        harness.setHand(player1, List.of(blast));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, blast.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(blast.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot foretell during an opponent's turn")
    void cannotForetellOnOpponentsTurn() {
        WarhornBlast blast = new WarhornBlast();
        harness.setHand(player2, List.of(blast));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.foretell(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(blast);
        assertThat(gd.findExiledCard(blast.getId())).isNull();
    }

    @Test
    @DisplayName("A foretold instant can be cast during an opponent's later turn")
    void castsForetoldInstantDuringOpponentsTurn() {
        Permanent firstPup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        Permanent secondPup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        WarhornBlast blast = new WarhornBlast();
        harness.setHand(player1, List.of(blast));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromExile(player1, blast.getId());
        harness.passBothPriorities();

        assertThat(firstPup.getPowerModifier()).isEqualTo(2);
        assertThat(firstPup.getToughnessModifier()).isEqualTo(1);
        assertThat(secondPup.getPowerModifier()).isEqualTo(2);
        assertThat(secondPup.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.findExiledCard(blast.getId())).isNull();
        harness.assertInGraveyard(player1, "Warhorn Blast");
    }
}
