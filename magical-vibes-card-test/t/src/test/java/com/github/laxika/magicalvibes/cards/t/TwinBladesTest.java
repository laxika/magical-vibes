package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KyoshiWarriors;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinBlades.class, KyoshiWarriors.class})
class TwinBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Twin Blades attaches it and grants double strike")
    void enteringAttachesAndGrantsDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        castTwinBlades(creature);

        Permanent twinBlades = findPermanent(player1, "Twin Blades");
        assertThat(twinBlades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Twin Blades's enter-the-battlefield double strike grant expires at end of turn")
    void doubleStrikeExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        castTwinBlades(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip attaches Twin Blades and keeps its static bonus")
    void equipAttachesAndKeepsStaticBonus() {
        Permanent twinBlades = harness.addToBattlefieldAndReturn(player1, new TwinBlades());
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(twinBlades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Twin Blades cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new KyoshiWarriors());
        Permanent opponentCreature = addCreatureReady(player2, new KyoshiWarriors());
        harness.setHand(player1, List.of(new TwinBlades()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Twin Blades").getAttachedTo()).isEqualTo(ownCreature.getId());
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        castTwinBlades(creature);

        assertThat(findPermanent(player1, "Twin Blades").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new TwinBlades());
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void movingEquipmentMovesBonusButNotDoubleStrike() {
        Permanent firstCreature = addCreatureReady(player1, new KyoshiWarriors());
        castTwinBlades(firstCreature);
        Permanent secondCreature = addCreatureReady(player1, new KyoshiWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Twin Blades").getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void triggerGrantsDoubleStrikeEvenIfEquipmentLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        Permanent equipment = harness.enterBattlefieldAndReturn(player1, new TwinBlades());
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, equipment));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Twin Blades");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void triggerDoesNothingIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new KyoshiWarriors());
        Permanent equipment = harness.enterBattlefieldAndReturn(player1, new TwinBlades());
        harness.handlePermanentChosen(player1, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertInGraveyard(player1, "Kyoshi Warriors");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeCastWithoutAnyCreatures() {
        harness.setHand(player1, List.of(new TwinBlades()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Twin Blades").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castTwinBlades(Permanent target) {
        harness.setHand(player1, List.of(new TwinBlades()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
