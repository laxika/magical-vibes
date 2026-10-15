package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AegisAngel.class, RuneclawBear.class, DoomBlade.class, MindControl.class,
        Manalith.class, Naturalize.class})
class AegisAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants indestructible to another permanent, which then survives a destroy spell")
    void grantedPermanentSurvivesDestroy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bears.getId());
        resolveAllTriggers();

        doomBlade(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("The grant is not until end of turn — it survives cleanup")
    void grantSurvivesCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bears.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        doomBlade(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("The grant ends when Aegis Angel leaves the battlefield")
    void grantEndsWhenAngelLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bears.getId());
        resolveAllTriggers();

        UUID angelId = harness.getPermanentId(player1, "Aegis Angel");
        doomBlade(player1, angelId);
        harness.assertInGraveyard(player1, "Aegis Angel");

        doomBlade(player1, bears.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Aegis Angel cannot target itself — it never gains indestructible")
    void angelDoesNotGrantToItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bears.getId());
        resolveAllTriggers();

        UUID angelId = harness.getPermanentId(player1, "Aegis Angel");
        doomBlade(player1, angelId);

        harness.assertInGraveyard(player1, "Aegis Angel");
    }

    @Test
    @DisplayName("The ETB target must be a permanent other than Aegis Angel")
    void cannotTargetAnIllegalPermanent() {
        harness.setHand(player1, List.of(new AegisAngel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canProtectAnOpponentsPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castAngel(player1, bear.getId());
        resolveAllTriggers();

        doomBlade(player1, bear.getId());

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void canProtectANoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Manalith());
        castAngel(player1, artifact.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertOnBattlefield(player1, "Manalith");
    }

    @Test
    void grantDoesNotStartIfAngelLeavesBeforeTriggerResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bear.getId());
        harness.passBothPriorities();
        doomBlade(player1, harness.getPermanentId(player1, "Aegis Angel"));
        resolveAllTriggers();

        doomBlade(player1, bear.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    void grantEndsWhenOpponentGainsControlOfAngel() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bear.getId());
        resolveAllTriggers();
        UUID angelId = harness.getPermanentId(player1, "Aegis Angel");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, angelId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aegis Angel");

        doomBlade(player2, bear.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @CardUsed({RayOfCommand.class})
    void grantDoesNotStartIfAngelIsStolenBeforeTriggerResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAngel(player1, bear.getId());
        harness.passBothPriorities();
        UUID angelId = harness.getPermanentId(player1, "Aegis Angel");
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, angelId);
        harness.assertOnBattlefield(player2, "Aegis Angel");
        resolveAllTriggers();

        doomBlade(player1, bear.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    private void castAngel(Player player, UUID targetId) {
        harness.setHand(player, List.of(new AegisAngel()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castCreature(player, 0, 0, targetId);
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
