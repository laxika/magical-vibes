package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisenReef.class, AirElemental.class, Forest.class, GrizzlyBears.class, Conspiracy.class})
class RisenReefTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry offers a top land and puts it onto the battlefield tapped")
    void ownEntryPutsLandOntoBattlefieldTapped() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        castRisenReef();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(topLand);
        assertThat(land).isNotNull();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining a top land puts it into hand")
    void declinedLandGoesToHand() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        castRisenReef();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topLand.getId()));
    }

    @Test
    @DisplayName("Another Elemental entering also triggers the ability")
    void anotherElementalTriggers() {
        RisenReef reef = new RisenReef();
        harness.addToBattlefield(player1, reef);
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(topLand)).isNotNull();
    }

    @Test
    @DisplayName("A nonland top card goes into hand without a may choice")
    void nonlandTopCardGoesToHand() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        castRisenReef();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("A non-Elemental creature entering does not trigger it")
    void nonElementalDoesNotTrigger() {
        RisenReef reef = new RisenReef();
        harness.addToBattlefield(player1, reef);
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
    }

    @Test
    @DisplayName("An empty library produces no choice and does not cause a failed draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castRisenReef();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Risen Reef");
    }

    @Test
    @DisplayName("An opposing Elemental entering does not trigger the ability")
    void opposingElementalDoesNotTrigger() {
        harness.addToBattlefield(player1, new RisenReef());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.enterBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topLand);
    }

    @Test
    @DisplayName("A second Reef triggers both Reefs, each using the current top card")
    void secondReefTriggersBothReefs() {
        harness.addToBattlefield(player1, new RisenReef());
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        castRisenReef();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand);
        assertThat(findPermanent(secondLand).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Its own entry triggers even when Conspiracy replaces its Elemental type")
    void ownEntryTriggersWhenNotAnElemental() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        castRisenReef();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanent(topLand).isTapped()).isTrue();
    }

    private void castRisenReef() {
        harness.castFromHand(player1, new RisenReef(), "{1}{G}{U}");
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
