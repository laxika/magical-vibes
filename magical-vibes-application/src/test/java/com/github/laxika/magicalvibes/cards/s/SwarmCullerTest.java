package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmCuller.class, GrizzlyBears.class, MindStone.class})
class SwarmCullerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped may sacrifice another creature and draw a card")
    void sacrificesCreatureAndDrawsCard() {
        Permanent culler = addCreatureReady(player1, new SwarmCuller());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(culler);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Becoming tapped may sacrifice another artifact and draw a card")
    void sacrificesArtifactAndDrawsCard() {
        Permanent culler = addCreatureReady(player1, new SwarmCuller());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(culler);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the sacrifice draws no card")
    void decliningSacrificeDoesNothing() {
        Permanent culler = addCreatureReady(player1, new SwarmCuller());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(culler);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(culler, sacrifice);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The source cannot be sacrificed for its own ability")
    void cannotSacrificeItself() {
        Permanent culler = addCreatureReady(player1, new SwarmCuller());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(culler);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(culler);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger Swarm Culler")
    void tappingAnotherPermanentDoesNotTrigger() {
        addCreatureReady(player1, new SwarmCuller());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        other.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, other));

        assertThat(gd.stack).isEmpty();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.passBothPriorities();
    }
}
