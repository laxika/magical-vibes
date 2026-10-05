package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeymasterRogue.class, FrilledOculus.class, PropheticPrism.class})
class KeymasterRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Keymaster Rogue cannot be blocked")
    void cannotBeBlocked() {
        Permanent keymaster = addCreatureReady(player1, new KeymasterRogue());
        keymaster.setAttacking(true);
        addCreatureReady(player2, new FrilledOculus());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Entering prompts for a creature you control and returns the choice to its owner's hand")
    void entersAndReturnsChosenCreature() {
        Permanent oculus = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        Permanent opponentOculus = harness.addToBattlefieldAndReturn(player2, new FrilledOculus());

        harness.castFromHand(player1, new KeymasterRogue(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(oculus.getId())
                .doesNotContain(opponentOculus.getId());

        harness.handlePermanentChosen(player1, oculus.getId());

        harness.assertNotOnBattlefield(player1, "Frilled Oculus");
        harness.assertInHand(player1, "Frilled Oculus");
    }

    @Test
    @DisplayName("Entering must return Keymaster Rogue when it is the only creature you control")
    void returnsItselfWhenItIsTheOnlyCreature() {
        harness.castFromHand(player1, new KeymasterRogue(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        var keymasterId = harness.getPermanentId(player1, "Keymaster Rogue");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(keymasterId);

        harness.handlePermanentChosen(player1, keymasterId);

        harness.assertNotOnBattlefield(player1, "Keymaster Rogue");
        harness.assertInHand(player1, "Keymaster Rogue");
    }

    @Test
    @DisplayName("A noncreature permanent cannot be chosen for the mandatory return")
    void cannotReturnNoncreaturePermanent() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.castFromHand(player1, new KeymasterRogue(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        var keymasterId = harness.getPermanentId(player1, "Keymaster Rogue");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(keymasterId)
                .doesNotContain(prism.getId());
        harness.handlePermanentChosen(player1, keymasterId);

        harness.assertInHand(player1, "Keymaster Rogue");
        harness.assertOnBattlefield(player1, "Prophetic Prism");
    }

    @Test
    @DisplayName("The return choice uses creatures controlled when the trigger resolves")
    void canChooseCreatureAddedAfterTriggerIsStacked() {
        harness.castFromHand(player1, new KeymasterRogue(), "{3}{U}");
        harness.passBothPriorities();

        Permanent oculus = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(oculus.getId());
        harness.handlePermanentChosen(player1, oculus.getId());

        harness.assertInHand(player1, "Frilled Oculus");
        harness.assertOnBattlefield(player1, "Keymaster Rogue");
    }

    @Test
    @DisplayName("A creature controlled by you but owned by the opponent returns to their hand")
    void returnsCreatureToItsOwnerRatherThanController() {
        FrilledOculus card = new FrilledOculus();
        card.setOwnerId(player2.getId());
        Permanent oculus = harness.addToBattlefieldAndReturn(player1, card);

        harness.castFromHand(player1, new KeymasterRogue(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, oculus.getId());

        harness.assertNotOnBattlefield(player1, "Frilled Oculus");
        harness.assertNotInHand(player1, "Frilled Oculus");
        harness.assertInHand(player2, "Frilled Oculus");
        harness.assertOnBattlefield(player1, "Keymaster Rogue");
    }
}
