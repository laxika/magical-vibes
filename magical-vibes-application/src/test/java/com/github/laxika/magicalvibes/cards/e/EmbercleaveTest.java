package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
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

@CardUsed({Embercleave.class, KnightOfTheKeep.class})
class EmbercleaveTest extends BaseCardTest {

    @Test
    @DisplayName("Embercleave costs one less for each attacking creature you control")
    void attackingCreaturesReduceCastingCost() {
        Permanent firstAttacker = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent secondAttacker = addCreatureReady(player1, new KnightOfTheKeep());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, firstAttacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flash allows Embercleave to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1, new KnightOfTheKeep());
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Embercleave enters attached to the targeted creature")
    void entersAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new KnightOfTheKeep());
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent embercleave = findPermanent(player1, "Embercleave");
        assertThat(embercleave.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void excessAttackersDoNotReduceColoredMana() {
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player1, new KnightOfTheKeep()).setAttacking(true);
        }
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void attackersCannotPayForMissingRedMana() {
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new KnightOfTheKeep()).setAttacking(true);
        }
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingAttackersAndNonattackingCreaturesDoNotReduceCost() {
        addCreatureReady(player1, new KnightOfTheKeep());
        addCreatureReady(player2, new KnightOfTheKeep()).setAttacking(true);
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canResolveWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Embercleave").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryTriggerChoosesTargetAfterSpellResolves() {
        Permanent creature = addCreatureReady(player1, new KnightOfTheKeep());
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Embercleave");
        assertThat(equipment.getAttachedTo()).isNull();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void entryTriggerCannotChooseOpponentsCreature() {
        Permanent creature = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent opponent = addCreatureReady(player2, new KnightOfTheKeep());
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Embercleave").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void entryTriggerDoesNotAttachToAnotherCreatureIfTargetLeaves() {
        Permanent target = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent other = addCreatureReady(player1, new KnightOfTheKeep());
        harness.setHand(player1, List.of(new Embercleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Embercleave").getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equipForThreeTransfersAllBonuses() {
        Permanent first = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent second = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Embercleave());
        equipment.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, 0, null, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent opponent = addCreatureReady(player2, new KnightOfTheKeep());
        harness.addToBattlefield(player1, new Embercleave());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashDoesNotAllowEquipDuringCombat() {
        Permanent creature = addCreatureReady(player1, new KnightOfTheKeep());
        harness.addToBattlefield(player1, new Embercleave());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
