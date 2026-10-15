package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloakAndDaggerEntwined.class, Forest.class, GrizzlyBears.class, Shock.class})
class CloakAndDaggerEntwinedTest extends BaseCardTest {

    @Test
    void exilesAnOptionalNonlandCardFromTheRevealedHand() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock, new Forest()));

        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Exile a nonland card from their hand.");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(shock);
    }

    @Test
    void creatureModeOnlyAllowsTheChosenOpponentsCreatureAndReturnsItWhenSourceLeaves() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(RuntimeException.class);
        harness.handlePermanentChosen(player1, opponentBear.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Exile the chosen creature.");

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBear);
        Permanent source = findPermanent(player1, "Cloak and Dagger, Entwined");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Grizzly Bears");
    }

    @Test
    void handModeRemainsAvailableWhenTheOptionalCreatureLeavesBeforeResolution() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));

        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, opponentBear.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentBear));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Exile a nonland card from their hand.");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
    }

    private void castCloakAndDagger() {
        harness.castFromHand(player1, new CloakAndDaggerEntwined(), "{1}{W}{B}");
        harness.passBothPriorities();
    }

    @Test
    void handCardReturnsToHandWhenSourceLeaves() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Exile a nonland card from their hand.");
        harness.handleCardChosen(player1, 0);

        Permanent source = findPermanent(player1, "Cloak and Dagger, Entwined");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        assertThat(gd.playerHands.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
    }

    @Test
    void mayDeclineExileEvenWhenBothChoicesAreAvailable() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Exile a nonland card from their hand.");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).contains(shock);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void landsCannotBeChosenForHandExile() {
        harness.setHand(player2, List.of(new Forest(), new Shock()));
        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Exile a nonland card from their hand.");

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(RuntimeException.class);
        harness.handleCardChosen(player1, 1);
        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player2, "Shock");
    }

    @Test
    void noHandCardIsExiledIfSourceLeavesBeforeTriggerResolves() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent source = findPermanent(player1, "Cloak and Dagger, Entwined");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "Exile a nonland card from their hand.");
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.RevealedHandChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
    }

    @Test
    void noCreatureIsExiledIfSourceLeavesBeforeTriggerResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCloakAndDagger();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        Permanent source = findPermanent(player1, "Cloak and Dagger, Entwined");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "Exile the chosen creature.");
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
