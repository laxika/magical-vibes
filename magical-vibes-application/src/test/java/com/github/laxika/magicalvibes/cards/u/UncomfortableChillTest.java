package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncomfortableChill.class, GreenwoodSentinel.class})
class UncomfortableChillTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures opponents control -2/-0 and leaves your own creatures alone")
    void weakensOnlyOpponentCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent enemyBear = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyBear = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects creatures present at resolution but not those entering afterward")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0);

        Permanent beforeResolution = addCreatureReady(player2, new GreenwoodSentinel());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player2, new GreenwoodSentinel());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isZero();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Chills stack and can reduce power below zero without reducing toughness")
    void multipleChillsStack() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new UncomfortableChill(), new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Uses the spell controller to determine opponents and who draws")
    void worksForOtherPlayer() {
        Permanent enemyCreature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent ownCreature = addCreatureReady(player2, new GreenwoodSentinel());
        GreenwoodSentinel drawnCard = new GreenwoodSentinel();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new UncomfortableChill()));
        int opponentHandSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isZero();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSize);
    }
}
