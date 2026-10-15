package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingerflingSpider.class, SerraAngel.class, RuneclawBear.class, Unsummon.class})
class StingerflingSpiderTest extends BaseCardTest {

    private void castSpider() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StingerflingSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB may destroys the chosen flying creature")
    void etbDestroysTargetFlier() {
        UUID flierId = harness.addToBattlefieldAndReturn(player2, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("Declining the ETB may leaves the flying creature alive")
    void decliningMayLeavesFlierAlive() {
        UUID flierId = harness.addToBattlefieldAndReturn(player2, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("No may prompt when only non-flying creatures are on the battlefield")
    void noPromptWithoutFliers() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castSpider();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("The may prompt fires when a flying creature is available")
    void mayPromptFiresWithFlier() {
        UUID flierId = harness.addToBattlefieldAndReturn(player2, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The triggered ability can destroy its controller's flying creature")
    void canDestroyFriendlyFlier() {
        UUID flierId = harness.addToBattlefieldAndReturn(player1, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not resolve or offer a may choice after its target leaves")
    void targetLeavingPreventsResolution() {
        UUID flierId = harness.addToBattlefieldAndReturn(player2, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, flierId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Serra Angel");
        harness.assertNotInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("The triggered ability still destroys its target after the Spider leaves")
    void sourceLeavingDoesNotStopAbility() {
        UUID flierId = harness.addToBattlefieldAndReturn(player2, new SerraAngel()).getId();

        castSpider();
        harness.handlePermanentChosen(player1, flierId);
        UUID spiderId = harness.getPermanentId(player1, "Stingerfling Spider");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, spiderId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Stingerfling Spider");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.stack).isEmpty();
    }
}
