package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DefiantKhenra;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazakethTheFoulblooded.class, DefiantKhenra.class, Forest.class})
class RazakethTheFoulbloodedTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life, sacrifices another creature, and searches a card into hand")
    void abilityTutorsCardToHand() {
        harness.setLife(player1, 20);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities(); // resolve ability → library search prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Paid 2 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        // Other creature was sacrificed
        harness.assertNotOnBattlefield(player1, "Defiant Khenra");
        harness.assertInGraveyard(player1, "Defiant Khenra");
        // Tutored card is in hand
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate when Razaketh is the only creature (can't sacrifice itself)")
    void cannotSacrificeItself() {
        harness.setLife(player1, 20);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        harness.setLibrary(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        harness.setLife(player1, 1);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life and sacrifice are paid before the ability resolves")
    void paysCostsBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Defiant Khenra");
        harness.assertNotOnBattlefield(player1, "Defiant Khenra");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick and find a creature card")
    void activatesWhileTappedAndSummoningSick() {
        Permanent razaketh = harness.addToBattlefieldAndReturn(player1, new RazakethTheFoulblooded());
        razaketh.setTapped(true);
        razaketh.setSummoningSick(true);
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of(new DefiantKhenra()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Defiant Khenra");
        harness.assertOnBattlefield(player1, "Razaketh, the Foulblooded");
    }

    @Test
    @DisplayName("Empty library still permits activation and payment of both costs")
    void emptyLibraryStillPaysCosts() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Defiant Khenra");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player2, new DefiantKhenra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Defiant Khenra");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot decline to find a card in a nonempty unrestricted search")
    void cannotFailToFind() {
        harness.addToBattlefield(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new DefiantKhenra());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
