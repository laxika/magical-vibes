package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.NessianAsp;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoathsomeCatoblepas.class, NessianAsp.class, NessianCourser.class, LashOfTheWhip.class})
class LoathsomeCatoblepasTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability makes Loathsome Catoblepas must be blocked")
    void activatedAbilityMakesSourceMustBeBlocked() {
        Permanent catoblepas = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(catoblepas.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-be-blocked requirement wears off at end of turn")
    void mustBeBlockedRequirementWearsOff() {
        Permanent catoblepas = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(catoblepas.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("When Loathsome Catoblepas dies, an opponent creature gets -3/-3")
    void deathTriggerShrinksOpponentCreature() {
        UUID catoblepasId = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas()).getId();

        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianAsp());
        UUID targetId = target.getId();

        killCatoblepas(catoblepasId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Death trigger cannot target a creature controlled by its controller")
    void deathTriggerCannotTargetOwnCreature() {
        UUID catoblepasId = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas()).getId();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());

        killCatoblepas(catoblepasId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Death trigger kills a 3/3 opponent creature")
    void deathTriggerKillsThreeThreeCreature() {
        UUID catoblepasId = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas()).getId();

        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NessianCourser()).getId();

        killCatoblepas(catoblepasId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nessian Courser");
        harness.assertInGraveyard(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("One blocker satisfies the activated requirement even when more can block")
    void activatedRequirementRequiresOnlyOneBlocker() {
        Permanent catoblepas = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent otherBlocker = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        catoblepas.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A tapped defender is not required to block")
    void activatedRequirementAllowsNoBlocksWhenNoneAreAble() {
        Permanent catoblepas = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        blocker.tap();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        catoblepas.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Death trigger's -3/-3 wears off at end of turn")
    void deathTriggerModifierWearsOff() {
        UUID catoblepasId = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas()).getId();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianAsp());

        killCatoblepas(catoblepasId);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nessian Asp");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Own creature cannot be chosen when an opposing creature is available")
    void deathTriggerRejectsOwnCreatureWithLegalOpponentTarget() {
        UUID catoblepasId = harness.addToBattlefieldAndReturn(player1, new LoathsomeCatoblepas()).getId();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NessianAsp());

        killCatoblepas(catoblepasId);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-3);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-3);
    }

    private void killCatoblepas(UUID catoblepasId) {
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, catoblepasId);
    }
}
