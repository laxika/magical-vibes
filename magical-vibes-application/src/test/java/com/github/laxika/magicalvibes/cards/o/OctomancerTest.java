package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Octomancer.class, GrizzlyBears.class})
class OctomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Promised Gift gives the opponent an 8/8 blue Octopus")
    void promisedGiftCreatesOctopusForOpponent() {
        Octomancer octomancer = new Octomancer();
        harness.setHand(player1, List.of(octomancer));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGift(player1, 0, null, true);
        harness.passBothPriorities();

        Permanent octopus = findPermanent(player2, "Octopus");
        assertThat(octopus.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(octopus.getCard().getSubtypes()).containsExactly(CardSubtype.OCTOPUS);
        assertThat(octopus.getEffectivePower()).isEqualTo(8);
        assertThat(octopus.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Unpromised Gift does not give the opponent an Octopus")
    void unpromisedGiftDoesNotCreateOctopus() {
        harness.setHand(player1, List.of(new Octomancer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGift(player1, 0, null, false);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Octopus")).isEmpty();
    }

    @Test
    @DisplayName("At each end step, copies a creature token that entered this turn")
    void copiesEligibleTokenAtEndStep() {
        harness.addToBattlefield(player1, new Octomancer());
        Permanent token = addEnteredCreatureToken(player1);
        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(token.getId());

        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears).allSatisfy(bear -> {
            assertThat(bear.getCard().isToken()).isTrue();
            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Cannot choose an older creature token")
    void ignoresOlderCreatureToken() {
        harness.addToBattlefield(player1, new Octomancer());
        Card olderToken = new GrizzlyBears();
        olderToken.setToken(true);
        addCreatureReady(player1, olderToken);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    private Permanent addEnteredCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        return harness.enterBattlefieldAndReturn(player, token);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
