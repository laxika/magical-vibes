package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnlightenedAscetic.class, AngelicChorus.class, GrizzlyBears.class, Disperse.class})
class EnlightenedAsceticTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may destroys the chosen enchantment")
    void acceptingDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Enlightened Ascetic");
    }

    @Test
    @DisplayName("Declining the may leaves the enchantment on the battlefield")
    void decliningLeavesEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
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
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("Enters successfully with no trigger left on the stack when no enchantment is available")
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

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger does not resolve when its target leaves the battlefield")
    void targetLeavingBattlefieldPreventsResolution() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Angelic Chorus");
        harness.assertNotInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Enlightened Ascetic");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The trigger can destroy its target after Ascetic leaves the battlefield")
    void triggerResolvesIndependentlyOfSource() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castAscetic();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        UUID asceticId = harness.getPermanentId(player1, "Enlightened Ascetic");

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, asceticId);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Enlightened Ascetic");
        harness.assertNotOnBattlefield(player1, "Enlightened Ascetic");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    private void castAscetic() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new EnlightenedAscetic(), "{1}{W}");
    }
}
