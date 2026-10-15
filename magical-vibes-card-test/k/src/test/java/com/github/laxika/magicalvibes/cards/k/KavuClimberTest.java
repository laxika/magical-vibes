package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuClimber.class, Forest.class, Unsummon.class})
class KavuClimberTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kavu Climber puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Climber");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Entering without being cast draws for the entering creature's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard, new Forest()));

        harness.enterBattlefieldAndReturn(player2, new KavuClimber());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger resolves after Kavu Climber leaves the battlefield")
    void drawTriggerSurvivesSourceLeavingBattlefield() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard, new Forest()));
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Kavu Climber"));

        harness.assertNotOnBattlefield(player1, "Kavu Climber");
        harness.assertInHand(player1, "Kavu Climber");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
