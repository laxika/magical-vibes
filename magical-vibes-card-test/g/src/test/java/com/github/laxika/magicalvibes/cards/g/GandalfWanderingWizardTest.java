package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfWanderingWizard.class, GrizzlyBears.class, Unsummon.class})
class GandalfWanderingWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Ability shuffles Gandalf into his owner's library, then the owner draws three cards")
    void shufflesAndDrawsThreeCards() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1, new GandalfWanderingWizard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gandalf);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(Stream.concat(gd.playerHands.get(player1.getId()).stream(),
                gd.playerDecks.get(player1.getId()).stream()))
                .contains(gandalf.getCard());
    }

    @Test
    @DisplayName("A stolen Gandalf makes his owner draw, not his controller")
    void ownerDrawsAfterControlChanges() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player2, new GandalfWanderingWizard());
        gd.stolenCreatures.put(gandalf.getId(), player1.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(Stream.concat(gd.playerHands.get(player1.getId()).stream(),
                gd.playerDecks.get(player1.getId()).stream()))
                .contains(gandalf.getCard());
    }
    @Test
    @DisplayName("Owner still draws three cards if Gandalf leaves before resolution")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1, new GandalfWanderingWizard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gandalf));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Gandalf, Wandering Wizard");
    }

    @Test
    @DisplayName("Each activation draws three cards even after another activation shuffled Gandalf away")
    void repeatedActivationsEachDrawThree() {
        harness.addToBattlefield(player1, new GandalfWanderingWizard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gandalf, Wandering Wizard");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay three mana")
    void wardCountersSpellWithoutPayment() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1, new GandalfWanderingWizard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, gandalf.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gandalf, Wandering Wizard");
        harness.assertNotInHand(player1, "Gandalf, Wandering Wizard");
        harness.assertInGraveyard(player2, "Unsummon");
    }
}
