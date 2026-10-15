package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LaurineTheDiversion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamberThePlunderer.class, LaurineTheDiversion.class, Forest.class, GrizzlyBears.class, Shock.class})
class KamberThePlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Laurine")
    void partnerWithSearchesForLaurine() {
        Card decoy = new Forest();
        Card partner = new LaurineTheDiversion();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.enterBattlefieldAndReturn(player1, new KamberThePlunderer());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Laurine, the Diversion");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Laurine, the Diversion");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("An opponent creature dying gains 1 life and creates a Blood token")
    void opponentCreatureDeathGainsLifeAndCreatesBlood() {
        harness.addToBattlefield(player1, new KamberThePlunderer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card partner = new LaurineTheDiversion();
        Card decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(partner, decoy));
        harness.enterBattlefieldAndReturn(player1, new KamberThePlunderer());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(partner, decoy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void partnerSearchCanTargetControllerWithNoPartnerInLibrary() {
        Card decoy = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(decoy));
        harness.enterBattlefieldAndReturn(player1, new KamberThePlunderer());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(decoy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ownCreatureDeathDoesNotTriggerKamber() {
        harness.addToBattlefield(player1, new KamberThePlunderer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachOpponentCreatureThatDies() {
        harness.addToBattlefield(player1, new KamberThePlunderer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    void seesOpponentCreaturesDieSimultaneouslyWithIt() {
        var kamber = harness.addToBattlefieldAndReturn(player1, new KamberThePlunderer());
        var firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        kamber.setMarkedDamage(4);
        firstBear.setMarkedDamage(2);
        secondBear.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kamber, the Plunderer");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }
}
