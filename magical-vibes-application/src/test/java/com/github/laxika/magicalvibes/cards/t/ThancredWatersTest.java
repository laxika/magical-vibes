package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YshtolaNightsBlessed;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThancredWaters.class, Shock.class, GrizzlyBears.class, DoomBlade.class,
        YshtolaNightsBlessed.class, ActOfTreason.class, RayOfCommand.class})
class ThancredWatersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants another legendary permanent indestructible while Thancred remains controlled")
    void etbProtectsAnotherLegendaryPermanentWhileControlled() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent target = harness.addToBattlefieldAndReturn(player1, legendaryBears);

        castThancred(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        destroyThancred();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives Thancred indestructible until end of turn")
    void noncreatureSpellGivesIndestructibleUntilEndOfTurn() {
        Permanent thancred = harness.addToBattlefieldAndReturn(player1, new ThancredWaters());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thancred, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thancred, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Thancred's protection")
    void creatureSpellDoesNotTriggerProtection() {
        Permanent thancred = harness.addToBattlefieldAndReturn(player1, new ThancredWaters());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.hasKeyword(gd, thancred, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ETB ability only targets legendary permanents you control")
    void etbRequiresAnotherLegendaryPermanentYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThancredWaters()));
        addThancredMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary permanent you control");
    }

    @Test
    void protectionPersistsAcrossTurns() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaNightsBlessed());
        castThancred(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void protectionNeverStartsIfThancredLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaNightsBlessed());
        castThancred(target.getId());
        harness.passBothPriorities();

        destroyThancred();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void losingAndRegainingThancredBeforeTriggerResolvesPreventsProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaNightsBlessed());
        castThancred(target.getId());
        harness.passBothPriorities();
        UUID thancredId = harness.getPermanentId(player1, "Thancred Waters");

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, thancredId);
        harness.assertOnBattlefield(player2, "Thancred Waters");

        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, thancredId);
        harness.assertOnBattlefield(player1, "Thancred Waters");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void losingAndRegainingThancredDoesNotRestartProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaNightsBlessed());
        castThancred(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID thancredId = harness.getPermanentId(player1, "Thancred Waters");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, thancredId);

        harness.assertOnBattlefield(player2, "Thancred Waters");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thancred Waters");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void changingProtectedPermanentsControllerDoesNotEndProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaNightsBlessed());
        castThancred(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, target.getId());

        harness.assertOnBattlefield(player2, "Y'shtola, Night's Blessed");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotProtectThancred() {
        Permanent thancred = harness.addToBattlefieldAndReturn(player1, new ThancredWaters());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, thancred.getId());

        assertThat(gqs.hasKeyword(gd, thancred, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void flashAllowsCastingDuringOpponentsTurnWithoutLegalGuardTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThancredWaters()));
        addThancredMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thancred Waters");
        Permanent thancred = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Thancred Waters"));
        assertThat(gqs.hasKeyword(gd, thancred, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castThancred(UUID targetId) {
        harness.setHand(player1, List.of(new ThancredWaters()));
        addThancredMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addThancredMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void destroyThancred() {
        UUID thancredId = harness.getPermanentId(player1, "Thancred Waters");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, thancredId);
    }
}
