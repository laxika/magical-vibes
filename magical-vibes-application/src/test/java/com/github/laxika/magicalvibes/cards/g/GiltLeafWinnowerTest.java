package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.ElvishArchers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GiltLeafWinnowerTest extends BaseCardTest {

    private void castWinnower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiltLeafWinnower()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB may destroys a non-Elf creature with unequal power and toughness")
    void destroysNonElfWithUnequalPowerAndToughness() {
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        castWinnower();
        harness.handlePermanentChosen(player1, vanguardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Gilt-Leaf Winnower");
    }

    @Test
    @DisplayName("Declining the may leaves the creature alive")
    void decliningLeavesCreatureAlive() {
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        castWinnower();
        harness.handlePermanentChosen(player1, vanguardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Elves and creatures with equal power and toughness are not legal targets")
    void elvesAndEqualStatsCreaturesAreIllegalTargets() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new ElvishArchers());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID archersId = harness.getPermanentId(player2, "Elvish Archers");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castWinnower();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(archersId, bearsId);
        assertThat(choice.validPermanentIds()).contains(harness.getPermanentId(player2, "Elite Vanguard"));
    }

    @Test
    @DisplayName("No trigger goes on the stack when no legal target exists")
    void noTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiltLeafWinnower()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Gilt-Leaf Winnower");
    }
}
