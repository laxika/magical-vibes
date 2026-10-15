package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NovijenHeartOfProgress;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidespoutTyrant.class, SimicSignet.class, NovijenHeartOfProgress.class})
class TidespoutTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell returns a target creature to its owner's hand")
    void castingSpellReturnsTargetPermanent() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        Permanent target = addCreatureReady(player2, new TidespoutTyrant());

        harness.castFromHand(player1, new SimicSignet(), "{2}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Tidespout Tyrant");
    }

    @Test
    @DisplayName("The trigger can return a land")
    void castingSpellReturnsLand() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        harness.castFromHand(player1, new SimicSignet(), "{2}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Novijen, Heart of Progress");
    }

    @Test
    @DisplayName("An opponent casting an artifact spell does not trigger Tidespout Tyrant")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SimicSignet(), "{2}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Playing a land does not trigger Tidespout Tyrant")
    void playingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        harness.setHand(player1, List.of(new NovijenHeartOfProgress()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Novijen, Heart of Progress");
    }

    @Test
    @DisplayName("A controlled permanent returns to its owner's hand")
    void controlledPermanentReturnsToOwnersHand() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        SimicSignet targetCard = new SimicSignet();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.castFromHand(player1, new SimicSignet(), "{2}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Simic Signet");
        harness.assertNotInHand(player2, "Simic Signet");
        harness.assertNotOnBattlefield(player2, "Simic Signet");
    }

    @Test
    @DisplayName("The Tyrant can return itself before the triggering spell resolves")
    void canReturnItselfBeforeSpellResolves() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new TidespoutTyrant());

        harness.castFromHand(player1, new SimicSignet(), "{2}");
        harness.handlePermanentChosen(player1, tyrant.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tidespout Tyrant");
        harness.assertNotOnBattlefield(player1, "Tidespout Tyrant");
        harness.assertNotOnBattlefield(player1, "Simic Signet");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Simic Signet");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a creature spell triggers the Tyrant already on the battlefield")
    void creatureSpellTriggersExistingTyrant() {
        harness.addToBattlefield(player1, new TidespoutTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        harness.castFromHand(player1, new TidespoutTyrant(), "{5}{U}{U}{U}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Novijen, Heart of Progress");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Tyrant does not trigger from its own casting or entering the battlefield")
    void ownCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new NovijenHeartOfProgress());

        harness.castFromHand(player1, new TidespoutTyrant(), "{5}{U}{U}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tidespout Tyrant");
        harness.assertOnBattlefield(player2, "Novijen, Heart of Progress");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
