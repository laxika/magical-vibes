package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasCourser.class, ArgothianSprite.class, Forest.class})
class GaeasCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with three creature cards in your graveyard draws a card")
    void attackingWithThreeCreatureCardsDraws() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Noncreature cards and cards in an opponent's graveyard do not count")
    void onlyOwnCreatureCardsCount() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(), new Forest()));
        harness.setGraveyard(player2, List.of(new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The graveyard condition is checked again when the trigger resolves")
    void conditionMustStillBeMetOnResolution() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0));
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Reaching three creature cards after attacking does not create a trigger")
    void reachingThresholdAfterAttackingDoesNotDraw() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1,
                List.of(new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("More than three creature cards still draws exactly one card")
    void exceedingThresholdDrawsExactlyOneCard() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(),
                new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The attack trigger still draws after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        var courser = addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1,
                List.of(new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(courser);
        gd.playerGraveyards.get(player1.getId()).add(courser.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
