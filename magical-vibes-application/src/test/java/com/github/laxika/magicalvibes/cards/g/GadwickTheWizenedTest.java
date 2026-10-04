package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GadwickTheWizened.class, Forest.class, Opt.class, Spellbook.class})
class GadwickTheWizenedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws X cards")
    void entersAndDrawsXCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GadwickTheWizened()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Gadwick, the Wizened");
    }

    @Test
    @DisplayName("Casting a blue spell taps a target nonland permanent an opponent controls")
    void blueSpellTapsOpponentNonlandPermanent() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a nonblue spell does not trigger the tap ability")
    void nonblueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The tap ability cannot target a permanent its controller controls")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new Opt(), "{U}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new Opt(), "{U}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting Gadwick with X zero draws nothing and does not trigger its own tap ability")
    void zeroXDoesNotDrawOrTriggerItself() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GadwickTheWizened());
        harness.setHand(player1, List.of(new GadwickTheWizened()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Gadwick, the Wizened");
    }

    @Test
    @DisplayName("An opponent's blue spell does not trigger Gadwick")
    void opponentsBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.castFromHand(player2, new Opt(), "{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped permanent is a legal target and the trigger resolves before the blue spell")
    void alreadyTappedPermanentIsLegalTarget() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GadwickTheWizened());
        target.setTapped(true);

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting blue spells with no legal opposing nonland target does not require a target choice")
    void noLegalTargetsDoesNotRequireChoice() {
        harness.addToBattlefield(player1, new GadwickTheWizened());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Opt(), "{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Entering without being cast draws zero cards")
    void enteringWithoutBeingCastDrawsNothing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new GadwickTheWizened());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Gadwick, the Wizened");
    }
}
