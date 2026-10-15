package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.Terror;
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

@CardUsed({CoralSword.class, GrizzlyBears.class, Shatter.class, Terror.class})
class CoralSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it first strike")
    void entersAttachedAndGrantsFirstStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCoralSword(creature);

        Permanent sword = findPermanent(player1, "Coral Sword");
        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn while the equipped bonus remains")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCoralSword(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip attaches Coral Sword to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sword.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoralSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting Coral Sword during the opponent's turn")
    void canCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        castCoralSword(creature);

        assertThat(findPermanent(player1, "Coral Sword").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Coral Sword can resolve without any creature to attach to")
    void resolvesWithoutCreatures() {
        harness.setHand(player1, List.of(new CoralSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Coral Sword").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First strike is still granted if the Equipment leaves before its trigger resolves")
    void grantsFirstStrikeAfterEquipmentIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoralSword(), new Shatter()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent sword = findPermanent(player1, "Coral Sword");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.castInstant(player1, 0, sword.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Coral Sword");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The entry trigger does not attach to a different creature when its target dies")
    void entryTriggerDoesNotRetargetAfterTargetDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoralSword(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Coral Sword").getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat despite Coral Sword having flash")
    void equipCannotBeActivatedDuringCombat() {
        addSwordReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Coral Sword").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Moving the Equipment does not move or grant first strike")
    void firstStrikeStaysWithOriginalTargetAfterEquippingAnotherCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCoralSword(firstCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Coral Sword").getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castCoralSword(Permanent target) {
        harness.setHand(player1, List.of(new CoralSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private Permanent addSwordReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new CoralSword());
        sword.setSummoningSick(false);
        return sword;
    }
}
