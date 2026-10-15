package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenShortsword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheQueenOfDale.class, Forest.class, OrdinaryBear.class, DwarvenShortsword.class})
class TheQueenOfDaleTest extends BaseCardTest {

    @Test
    @DisplayName("Recruit triggers for an opponent's first noncreature spell each turn")
    void recruitsOnFirstNoncreatureSpellEachTurn() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new OrdinaryBear()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new OrdinaryBear(), new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        prepareTurn(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        prepareTurn(player2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ordinary Bear");
        assertThat(findPermanents(player1, "Human Soldier")).singleElement().satisfies(token ->
                assertThat(token.getCard().getSubtypes())
                        .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Recruit does not create a Soldier when the discarded card is a land")
    void doesNotRecruitForLandDiscard() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new OrdinaryBear()));
        harness.setHand(player2, List.of(new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void createsTokenDuringRecruitResolutionWithoutAnotherPriorityRound() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new OrdinaryBear()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Dwarven Shortsword");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Ordinary Bear");
    }

    @Test
    void doesNotTriggerForOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player2, List.of(new OrdinaryBear()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ordinary Bear");
    }

    @Test
    void doesNotTriggerForControllersNoncreatureSpell() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareTurn(player1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerForSecondNoncreatureSpellInSameTurn() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new OrdinaryBear()));
        harness.setHand(player2, List.of(new DwarvenShortsword(), new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        prepareTurn(player2);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Ordinary Bear");
    }

    @Test
    void canDiscardTheCardJustDrawnFromAnEmptyHand() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new OrdinaryBear()));
        harness.setHand(player2, List.of(new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Ordinary Bear");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ordinary Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    void triggersAgainOnANewTurn() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new DwarvenShortsword(), new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void remembersNoncreatureSpellCastBeforeQueenEntered() {
        harness.setHand(player2, List.of(new DwarvenShortsword(), new DwarvenShortsword()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TheQueenOfDale());
        prepareTurn(player2);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
