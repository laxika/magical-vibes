package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.h.Hammerheim;
import com.github.laxika.magicalvibes.cards.h.HundingGjornersen;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RapidFire.class, BarbaryApes.class, HundingGjornersen.class, Hammerheim.class,
        KoboldsOfKherKeep.class})
class RapidFireTest extends BaseCardTest {

    @Test
    @DisplayName("Before blockers are declared, the target gains first strike and rampage 2")
    void grantsFirstStrikeAndRampage() {
        Permanent target = addCreatureReady(player1, new BarbaryApes());
        castRapidFire(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        addCreatureReady(player2, new BarbaryApes());
        addCreatureReady(player2, new BarbaryApes());
        addCreatureReady(player2, new BarbaryApes());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Rapid Fire does not grant a second rampage ability")
    void doesNotGrantSecondRampage() {
        Permanent target = addCreatureReady(player1, new HundingGjornersen());
        castRapidFire(target);

        addCreatureReady(player2, new BarbaryApes());
        addCreatureReady(player2, new BarbaryApes());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rapid Fire can be cast before the first combat's blockers step")
    void canBeCastBeforeBlockersAreDeclared() {
        Permanent target = addCreatureReady(player1, new BarbaryApes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castRapidFire(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rapid Fire cannot be cast once blockers are declared")
    void cannotBeCastAfterBlockersAreDeclared() {
        Permanent target = addCreatureReady(player1, new BarbaryApes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        prepareRapidFireInHand();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rapid Fire cannot be cast before a later combat's blockers step")
    void cannotBeCastBeforeLaterCombat() {
        Permanent target = addCreatureReady(player1, new BarbaryApes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        harness.clearPriorityPassed();
        prepareRapidFireInHand();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The granted abilities last until end of turn")
    void grantedAbilitiesWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new BarbaryApes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castRapidFire(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();

        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rapid Fire cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        prepareRapidFireInHand();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRapidFire(Permanent target) {
        prepareRapidFireInHand();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareRapidFireInHand() {
        harness.setHand(player1, List.of(new RapidFire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
