package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StingerflingSpiderTest extends BaseCardTest {

    private void castSpider() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StingerflingSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
    }

    @Test
    @DisplayName("Accepting the ETB may destroys the chosen flying creature")
    void etbDestroysTargetFlier() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID flierId = harness.getPermanentId(player2, "Air Elemental");

        castSpider();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, flierId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("Declining the ETB may leaves the flying creature alive")
    void decliningMayLeavesFlierAlive() {
        harness.addToBattlefield(player2, new AirElemental());

        castSpider();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("No may prompt when only non-flying creatures are on the battlefield")
    void noPromptWithoutFliers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StingerflingSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stingerfling Spider");
    }

    @Test
    @DisplayName("The may prompt fires when a flying creature is available")
    void mayPromptFiresWithFlier() {
        harness.addToBattlefield(player2, new AirElemental());

        castSpider();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
