package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.setHand(player1, List.of(new CloakAndDaggerEntwined()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
