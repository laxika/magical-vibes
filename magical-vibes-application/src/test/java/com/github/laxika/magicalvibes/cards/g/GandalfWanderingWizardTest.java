package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfWanderingWizard.class, GrizzlyBears.class})
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
}
