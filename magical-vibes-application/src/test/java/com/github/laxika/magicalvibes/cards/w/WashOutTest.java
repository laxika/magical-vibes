package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.s.SlinkingSerpent;
import com.github.laxika.magicalvibes.cards.s.SwayOfIllusion;
import com.github.laxika.magicalvibes.cards.v.ViashinoGrappler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WashOut.class, SwayOfIllusion.class, RagingKavu.class, SlinkingSerpent.class,
        Forest.class, ViashinoGrappler.class})
class WashOutTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wash Out awaits the caster's color choice")
    void resolvingAwaitsColorChoice() {
        harness.addToBattlefield(player2, new RagingKavu());
        castWashOut();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Returns every permanent of the chosen color and leaves other colors alone")
    void returnsPermanentsOfChosenColor() {
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new RagingKavu());
        harness.addToBattlefield(player2, new SlinkingSerpent());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new ViashinoGrappler());
        castWashOut();

        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Slinking Serpent");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Viashino Grappler");
        harness.assertInHand(player1, "Raging Kavu");
        harness.assertInHand(player2, "Raging Kavu");
        harness.assertInGraveyard(player1, "Wash Out");
    }

    @Test
    @DisplayName("Returns no permanents when the chosen color is absent")
    void absentColorReturnsNothing() {
        harness.addToBattlefield(player2, new ViashinoGrappler());
        castWashOut();

        harness.handleListChoice(player1, "GREEN");

        harness.assertOnBattlefield(player2, "Viashino Grappler");
        harness.assertInGraveyard(player1, "Wash Out");
    }

    @Test
    @DisplayName("Uses a permanent's effective color when choosing what to return")
    void returnsPermanentByEffectiveColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new SwayOfIllusion(), new WashOut()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertInHand(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Returns a matching permanent to its owner's hand even when controlled by another player")
    void returnsPermanentToItsOwnerHand() {
        RagingKavu ownedByPlayer1 = new RagingKavu();
        ownedByPlayer1.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ownedByPlayer1);
        castWashOut();

        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertInHand(player1, "Raging Kavu");
        harness.assertNotInHand(player2, "Raging Kavu");
    }

    private void castWashOut() {
        harness.setHand(player1, List.of(new WashOut()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
