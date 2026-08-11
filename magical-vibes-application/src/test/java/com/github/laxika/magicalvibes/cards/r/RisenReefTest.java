package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
    }

    private void castRisenReef() {
        harness.setHand(player1, List.of(new RisenReef()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
