package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaseballBat.class, GrizzlyBears.class})
class BaseballBatTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Baseball Bat attaches it to a target creature you control")
    void enteringAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BaseballBat()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Baseball Bat");
        assertThat(bat.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature taps up to one target creature")
    void attackingTapsTargetCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new BaseballBat());
        bat.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                        .validPermanentIds())
                .contains(attacker.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger can be declined")
    void attackTriggerCanBeDeclined() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new BaseballBat());
        bat.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equip moves the attachment and bonus to the new creature for three mana")
    void equipMovesAttachmentAndBonus() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new BaseballBat());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bat.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, replacement.getId());

        assertThat(bat.getAttachedTo()).isEqualTo(original.getId());
        harness.passBothPriorities();

        assertThat(bat.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Baseball Bat can be cast without a creature to attach to")
    void canEnterWithoutCreature() {
        harness.setHand(player1, List.of(new BaseballBat()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Baseball Bat").getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        harness.addToBattlefield(player1, new BaseballBat());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Baseball Bat").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRejectsInsufficientMana() {
        harness.addToBattlefield(player1, new BaseballBat());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Baseball Bat").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new BaseballBat());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Baseball Bat").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Attacking with an unequipped creature does not trigger Baseball Bat")
    void unequippedAttackerDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new BaseballBat());
        bat.setAttachedTo(equipped.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }
}
