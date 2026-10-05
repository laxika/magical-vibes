package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CaseOfThePilferedProof;
import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OfficiousInterrogation.class, GraniteWitness.class, CaseOfThePilferedProof.class})
class OfficiousInterrogationTest extends BaseCardTest {

    @Test
    void investigatesForCreaturesControlledByTheTargetPlayer() {
        harness.addToBattlefield(player1, new GraniteWitness());
        harness.addToBattlefield(player2, new GraniteWitness());
        harness.addToBattlefield(player2, new GraniteWitness());

        cast(List.of(player2.getId()), 1);

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void sumsCreaturesControlledByAllChosenPlayers() {
        harness.addToBattlefield(player1, new GraniteWitness());
        harness.addToBattlefield(player2, new GraniteWitness());
        harness.addToBattlefield(player2, new GraniteWitness());

        cast(List.of(player2.getId(), player1.getId()), 2);

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    void canChooseNoTargetPlayers() {
        cast(List.of(), 1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void rejectsTwoPlayersWithoutTheAdditionalColoredMana() {
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotChooseTheSamePlayerTwice() {
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetACreatureInsteadOfAPlayer() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsCreaturesAtResolutionRatherThanAtCasting() {
        harness.addToBattlefield(player2, new GraniteWitness());
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.addToBattlefield(player2, new GraniteWitness());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void createsNoCluesForAPlayerWithoutCreatures() {
        harness.addToBattlefield(player2, new CaseOfThePilferedProof());

        cast(List.of(player2.getId()), 1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void eachInvestigationIsASeparateTokenCreationEvent() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        harness.addToBattlefield(player1, new GraniteWitness());
        harness.addToBattlefield(player1, new GraniteWitness());
        harness.addToBattlefield(player1, new GraniteWitness());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Case of the Pilfered Proof").isSolved()).isTrue();
        harness.addToBattlefield(player2, new GraniteWitness());
        harness.addToBattlefield(player2, new GraniteWitness());

        cast(List.of(player2.getId()), 1);

        assertThat(findPermanents(player1, "Clue")).hasSize(4);
    }

    private void cast(List<UUID> targetPlayerIds, int manaPerColor) {
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, manaPerColor);
        harness.addMana(player1, ManaColor.BLUE, manaPerColor);
        harness.castAndResolveInstant(player1, 0, targetPlayerIds);
    }
}
