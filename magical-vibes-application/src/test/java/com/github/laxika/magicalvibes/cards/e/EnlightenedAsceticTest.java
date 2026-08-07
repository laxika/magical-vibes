package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnlightenedAsceticTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may destroys the chosen enchantment")
    void acceptingDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities(); // resolve creature spell → ETB trigger
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Enlightened Ascetic");
    }

    @Test
    @DisplayName("Declining the may leaves the enchantment on the battlefield")
    void decliningLeavesEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        castAscetic();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Enlightened Ascetic");
    }

    @Test
    @DisplayName("Can destroy an enchantment its own controller controls")
    void canDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player1, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities(); // resolve creature → ETB + Angelic Chorus lifegain trigger
        harness.passBothPriorities(); // resolve Angelic Chorus trigger
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("No trigger when there is no enchantment to destroy")
    void noTriggerWithoutEnchantment() {
        castAscetic();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Enlightened Ascetic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot be chosen as the target")
    void creatureIsNotALegalTarget() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        castAscetic();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAscetic() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EnlightenedAscetic()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
    }
}
