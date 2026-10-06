package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.o.OpalGuardian;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.r.Reiterate;
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

@CardUsed({ReturnToDust.class, OpalGuardian.class, PrismaticLens.class, BenalishCavalry.class, Reiterate.class})
class ReturnToDustTest extends BaseCardTest {

    private void prepareReturnToDust(TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.setHand(player1, List.of(new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castReturnToDust(TurnStep step, List<UUID> targetIds) {
        prepareReturnToDust(step);
        harness.castInstant(player1, 0, targetIds);
    }

    private void castAndResolveReturnToDust(TurnStep step, List<UUID> targetIds) {
        prepareReturnToDust(step);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    @Test
    @DisplayName("Exiles both targets when cast during the controller's main phase")
    void exilesBothTargetsDuringMainPhase() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Prismatic Lens");
        UUID enchantmentId = harness.getPermanentId(player2, "Opal Guardian");
        castAndResolveReturnToDust(TurnStep.PRECOMBAT_MAIN, List.of(artifactId, enchantmentId));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact, enchantment);
    }

    @Test
    @DisplayName("Exiles only the mandatory target when no second target is chosen")
    void secondTargetCanBeOmitted() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Prismatic Lens");
        castAndResolveReturnToDust(TurnStep.PRECOMBAT_MAIN, List.of(artifactId));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Does not exile the optional target outside the controller's main phase")
    void optionalTargetDoesNotResolveOutsideMainPhase() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Prismatic Lens");
        UUID enchantmentId = harness.getPermanentId(player2, "Opal Guardian");
        castAndResolveReturnToDust(TurnStep.UPKEEP, List.of(artifactId, enchantmentId));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Rejects a creature as a target")
    void rejectsCreatureTarget() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        UUID creatureId = harness.getPermanentId(player2, "Benalish Cavalry");

        prepareReturnToDust(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects the same permanent as both targets")
    void rejectsSamePermanentAsBothTargets() {
        harness.addToBattlefield(player2, new PrismaticLens());
        UUID artifactId = harness.getPermanentId(player2, "Prismatic Lens");

        assertThatThrownBy(() -> castReturnToDust(TurnStep.PRECOMBAT_MAIN, List.of(artifactId, artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles both targets during the controller's postcombat main phase")
    void exilesBothTargetsDuringPostcombatMainPhase() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, enchantment).getId();

        castAndResolveReturnToDust(TurnStep.POSTCOMBAT_MAIN, List.of(artifactId, enchantmentId));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact, enchantment);
    }

    @Test
    @DisplayName("Does not exile the optional target during an opponent's main phase")
    void optionalTargetIsNotExiledDuringOpponentsMainPhase() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, enchantment).getId();
        prepareReturnToDust(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player1, 0, List.of(artifactId, enchantmentId));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
        harness.assertOnBattlefield(player2, "Opal Guardian");
    }

    @Test
    @DisplayName("Still exiles the optional target if the first target leaves before resolution")
    void exilesSecondTargetWhenFirstTargetLeavesBattlefield() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, enchantment).getId();
        prepareReturnToDust(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ReturnToDust(), new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(artifactId, enchantmentId));
        harness.castAndResolveInstant(player1, 0, List.of(artifactId));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact, enchantment);
    }

    @Test
    @DisplayName("A copy does not get Return to Dust's main-phase bonus")
    void copyDoesNotGetMainPhaseBonus() {
        PrismaticLens artifact = new PrismaticLens();
        OpalGuardian enchantment = new OpalGuardian();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Prismatic Lens");
        UUID enchantmentId = harness.getPermanentId(player2, "Opal Guardian");
        ReturnToDust returnToDust = new ReturnToDust();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(returnToDust, new Reiterate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, List.of(artifactId, enchantmentId));
        harness.castInstant(player1, 0, returnToDust.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
    }
}
