package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AegisAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants indestructible to another permanent, which then survives a destroy spell")
    void grantedPermanentSurvivesDestroy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAngel(player1, bears.getId());
        harness.passBothPriorities(); // resolve the Angel
        harness.passBothPriorities(); // resolve the ETB grant

        doomBlade(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The grant is not until end of turn — it survives cleanup")
    void grantSurvivesCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAngel(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        doomBlade(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The grant ends when Aegis Angel leaves the battlefield")
    void grantEndsWhenAngelLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAngel(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angel = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aegis Angel"))
                .findFirst().orElseThrow();
        doomBlade(player1, angel.getId());
        harness.assertInGraveyard(player1, "Aegis Angel");

        doomBlade(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Aegis Angel cannot target itself — it never gains indestructible")
    void angelDoesNotGrantToItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAngel(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angel = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aegis Angel"))
                .findFirst().orElseThrow();
        doomBlade(player1, angel.getId());

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

    private void castAngel(Player player, UUID targetId) {
        harness.setHand(player, List.of(new AegisAngel()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castCreature(player, 0, 0, targetId);
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }
}
