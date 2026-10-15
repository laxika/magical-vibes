package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshasFavor;
import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.f.FieryFall;
import com.github.laxika.magicalvibes.cards.m.MartialCoup;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.cards.r.RhoxBodyguard;
import com.github.laxika.magicalvibes.cards.s.ScepterOfDominance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinOutlander.class, AvenSquire.class, GrizzlyBears.class,
        RhoxBodyguard.class, PathToExile.class, FieryFall.class, AshasFavor.class,
        MartialCoup.class, ScepterOfDominance.class})
class GoblinOutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("White creature cannot block Goblin Outlander")
    void whiteCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AvenSquire());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Goblin Outlander")
    void greenCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin Outlander takes no combat damage from a green and white creature")
    void takesNoDamageFromWhite() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RhoxBodyguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GoblinOutlander());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The white source's damage is prevented even though it is also green.
        harness.assertOnBattlefield(player2, "Goblin Outlander");
    }

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent outlander = harness.addToBattlefieldAndReturn(player2, new GoblinOutlander());
        outlander.setSummoningSick(false);

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent outlander = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        outlander.setSummoningSick(false);

        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, outlander.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fiery Fall");
    }

    @Test
    @DisplayName("Red spell damage is not prevented by protection from white")
    void redSpellDamageIsNotPrevented() {
        Permanent outlander = harness.addToBattlefieldAndReturn(player2, new GoblinOutlander());
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, outlander.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Outlander");
    }

    @Test
    @DisplayName("A green and white creature cannot block Goblin Outlander")
    void multicoloredWhiteCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new RhoxBodyguard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection prevents the controller's white Aura from targeting Goblin Outlander")
    void ownWhiteAuraCannotTarget() {
        Permanent outlander = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        harness.addToBattlefield(player1, new AvenSquire());
        harness.setHand(player1, List.of(new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An attached white Aura goes to the graveyard as a state-based action")
    void attachedWhiteAuraIsRemoved() {
        Permanent outlander = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AshasFavor());
        aura.setAttachedTo(outlander.getId());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Asha's Favor");
        harness.assertOnBattlefield(player1, "Goblin Outlander");
    }

    @Test
    @DisplayName("An ability from a white artifact cannot target Goblin Outlander")
    void whiteArtifactAbilityCannotTarget() {
        harness.addToBattlefield(player1, new ScepterOfDominance());
        Permanent outlander = harness.addToBattlefieldAndReturn(player2, new GoblinOutlander());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white does not prevent untargeted destruction by a white spell")
    void whiteSweeperStillDestroys() {
        harness.addToBattlefield(player2, new GoblinOutlander());
        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Outlander");
    }
}
