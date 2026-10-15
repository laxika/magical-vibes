package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GnollHunter;
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

@CardUsed({DuelingRapier.class, GnollHunter.class})
class DuelingRapierTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it +2/+0")
    void entersAttachedAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rapier = findPermanent(player1, "Dueling Rapier");
        assertThat(rapier.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {4} attaches Dueling Rapier to a creature you control")
    void equipAttachesToCreature() {
        Permanent rapier = harness.addToBattlefieldAndReturn(player1, new DuelingRapier());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rapier.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("ETB attach cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new GnollHunter());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GnollHunter());
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastWithoutCreatures() {
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dueling Rapier").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashAllowsCastingDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dueling Rapier").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void choosesAttachmentTargetAfterSpellResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent rapier = findPermanent(player1, "Dueling Rapier");
        assertThat(rapier.getAttachedTo()).isNull();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(rapier.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(rapier.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equippingAnotherCreatureMovesPowerBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.setHand(player1, List.of(new DuelingRapier()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, null, second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dueling Rapier").getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void flashDoesNotAllowEquippingOutsideMainPhase() {
        harness.addToBattlefield(player1, new DuelingRapier());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new DuelingRapier());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnollHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipRequiresFourMana() {
        harness.addToBattlefield(player1, new DuelingRapier());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Dueling Rapier").getAttachedTo()).isNull();
    }
}
