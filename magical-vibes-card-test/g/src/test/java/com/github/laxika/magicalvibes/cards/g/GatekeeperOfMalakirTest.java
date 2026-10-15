package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatekeeperOfMalakir.class, GrizzlyBears.class, GiantSpider.class})
class GatekeeperOfMalakirTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not trigger")
    void withoutKickerDoesNotTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GatekeeperOfMalakir(), "{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the target player chooses a creature to sacrifice")
    void kickedTargetPlayerChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new GatekeeperOfMalakir()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, spider.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When kicked, nothing happens if the target player controls no creatures")
    void kickedTargetPlayerWithNoCreatures() {
        harness.setHand(player1, List.of(new GatekeeperOfMalakir()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gatekeeper of Malakir");
    }

    @Test
    @DisplayName("A kicked Gatekeeper can target its controller and sacrifice itself")
    void kickedCanTargetControllerAndSacrificeItself() {
        harness.setHand(player1, List.of(new GatekeeperOfMalakir()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castKickedCreature(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gatekeeper of Malakir");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gatekeeper of Malakir");
        harness.assertInGraveyard(player1, "Gatekeeper of Malakir");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the kicked ability")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.enterBattlefieldAndReturn(player1, new GatekeeperOfMalakir());

        harness.assertOnBattlefield(player1, "Gatekeeper of Malakir");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The target player's only creature is sacrificed automatically")
    void kickedSacrificesOnlyCreature() {
        harness.addToBattlefield(player2, new GatekeeperOfMalakir());
        harness.setHand(player1, List.of(new GatekeeperOfMalakir()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gatekeeper of Malakir");
        harness.assertInGraveyard(player2, "Gatekeeper of Malakir");
        harness.assertOnBattlefield(player1, "Gatekeeper of Malakir");
        assertThat(gd.stack).isEmpty();
    }
}
