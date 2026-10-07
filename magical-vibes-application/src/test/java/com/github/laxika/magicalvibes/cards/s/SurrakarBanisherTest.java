package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakarBanisher.class, WalkingAtlas.class, EverflowingChalice.class})
class SurrakarBanisherTest extends BaseCardTest {

    private Permanent tappedAtlas(Player controller) {
        Permanent atlas = harness.addToBattlefieldAndReturn(controller, new WalkingAtlas());
        atlas.tap();
        return atlas;
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SurrakarBanisher(), "{4}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a tapped opponent creature to its owner's hand")
    void etbBouncesTappedOpponentCreature() {
        UUID atlasId = tappedAtlas(player2).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, atlasId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Walking Atlas");
        harness.assertInHand(player2, "Walking Atlas");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("ETB can return a tapped creature you control")
    void etbBouncesTappedOwnCreature() {
        UUID atlasId = tappedAtlas(player1).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, atlasId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Walking Atlas");
        harness.assertInHand(player1, "Walking Atlas");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("Declining the may leaves the tapped creature on the battlefield")
    void decliningMayLeavesCreature() {
        UUID atlasId = tappedAtlas(player2).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, atlasId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Walking Atlas");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("An untapped creature is not a legal target")
    void untappedCreatureIsNotTargetable() {
        harness.addToBattlefield(player2, new WalkingAtlas());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Walking Atlas");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("A creature that untaps before resolution is no longer a legal target")
    void untappedBeforeResolutionIsNotReturned() {
        Permanent atlas = tappedAtlas(player2);
        castAndResolve();
        harness.handlePermanentChosen(player1, atlas.getId());

        atlas.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Walking Atlas");
        harness.assertNotInHand(player2, "Walking Atlas");
    }

    @Test
    @DisplayName("A tapped noncreature artifact is not a legal target")
    void tappedNoncreatureIsNotTargetable() {
        Permanent chalice = harness.addToBattlefieldAndReturn(player2, new EverflowingChalice());
        chalice.tap();
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Everflowing Chalice");
    }

    @Test
    @DisplayName("A creature controlled by another player returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        Permanent atlas = tappedAtlas(player1);
        gd.playerBattlefields.get(player1.getId()).remove(atlas);
        gd.playerBattlefields.get(player2.getId()).add(atlas);
        gd.stolenCreatures.put(atlas.getId(), player1.getId());
        castAndResolve();
        harness.handlePermanentChosen(player1, atlas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Walking Atlas");
        harness.assertInHand(player1, "Walking Atlas");
        harness.assertNotInHand(player2, "Walking Atlas");
        assertThat(gd.stack).isEmpty();
    }
}
