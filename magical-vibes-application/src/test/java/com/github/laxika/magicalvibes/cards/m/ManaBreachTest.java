package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.p.PygmyTroll;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaBreach.class, CityOfTraitors.class, PygmyTroll.class})
class ManaBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell prompts the caster to return a land they control")
    void castingPromptsCasterToBounceLand() {
        harness.addToBattlefield(player1, new ManaBreach());
        UUID landId = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors()).getId();

        harness.castFromHand(player1, new PygmyTroll(), "{1}{G}");
        // Resolve the Mana Breach triggered ability
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(landId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Chosen land is returned to its owner's hand")
    void chosenLandReturnsToHand() {
        harness.addToBattlefield(player1, new ManaBreach());
        UUID landId = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors()).getId();

        harness.castFromHand(player1, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, landId);

        harness.assertNotOnBattlefield(player1, "City of Traitors");
        harness.assertInHand(player1, "City of Traitors");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The caster chooses which of their lands to return")
    void casterChoosesAmongLands() {
        harness.addToBattlefield(player1, new ManaBreach());
        UUID firstLandId = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors()).getId();
        UUID secondLandId = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors()).getId();

        harness.castFromHand(player1, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstLandId, secondLandId);

        harness.handlePermanentChosen(player1, secondLandId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(firstLandId))
                .noneMatch(p -> p.getId().equals(secondLandId));
        harness.assertInHand(player1, "City of Traitors");
    }

    @Test
    @DisplayName("When the caster controls no lands, nothing happens")
    void noLandsNoBounce() {
        harness.addToBattlefield(player1, new ManaBreach());

        harness.castFromHand(player1, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Triggers for every player — the opponent bounces their own land")
    void opponentCastingBouncesOpponentsLand() {
        harness.addToBattlefield(player1, new ManaBreach());
        UUID landId = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors()).getId();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(landId);

        harness.handlePermanentChosen(player2, landId);

        harness.assertNotOnBattlefield(player2, "City of Traitors");
        harness.assertInHand(player2, "City of Traitors");
    }

    @Test
    @DisplayName("Only lands are offered when the caster controls other permanents")
    void nonlandsAreNotBounceChoices() {
        harness.addToBattlefield(player1, new ManaBreach());
        UUID landId = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors()).getId();
        harness.addToBattlefield(player1, new PygmyTroll());

        harness.castFromHand(player1, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(landId);
    }

    @Test
    @DisplayName("A controlled land is returned to its owner's hand")
    void controlledLandReturnsToOwnerHand() {
        harness.addToBattlefield(player1, new ManaBreach());
        CityOfTraitors land = new CityOfTraitors();
        land.setOwnerId(player1.getId());
        UUID landId = harness.addToBattlefieldAndReturn(player2, land).getId();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new PygmyTroll(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, landId);

        harness.assertNotOnBattlefield(player2, "City of Traitors");
        harness.assertInHand(player1, "City of Traitors");
        harness.assertNotInHand(player2, "City of Traitors");
    }
}
