package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.cards.n.NoblePurpose;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevoutWitness.class, FreshVolunteers.class, HengeGuardian.class, IronLance.class, NoblePurpose.class})
class DevoutWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W}, {T}, Discard: destroys target artifact")
    void destroysTargetArtifact() {
        Permanent witness = addReadyWitness(player1);
        Permanent target = addReadyArtifact(player2);
        prepareActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(witness.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Iron Lance");
        harness.assertInGraveyard(player2, "Iron Lance");
        harness.assertInGraveyard(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Can destroy an artifact creature")
    void destroysTargetArtifactCreature() {
        addReadyWitness(player1);
        Permanent target = addCreatureReady(player2, new HengeGuardian());
        prepareActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Henge Guardian");
        harness.assertInGraveyard(player2, "Henge Guardian");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its controller")
    void destroysOwnArtifact() {
        addReadyWitness(player1);
        Permanent target = addReadyArtifact(player1);
        prepareActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Lance");
        harness.assertInGraveyard(player1, "Iron Lance");
    }

    @Test
    @DisplayName("{1}{W}, {T}, Discard: destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyWitness(player1);
        Permanent target = addReadyEnchantment(player2);
        prepareActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Noble Purpose");
        harness.assertInGraveyard(player2, "Noble Purpose");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyWitness(player1);
        Permanent creature = addCreatureReady(player2, new FreshVolunteers());
        prepareMana();
        harness.setHand(player1, List.of(new FreshVolunteers()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyWitness(player1);
        Permanent target = addReadyArtifact(player2);
        prepareMana();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareActivation() {
        prepareMana();
        harness.setHand(player1, List.of(new FreshVolunteers()));
    }

    private void prepareMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyWitness(Player player) {
        return addCreatureReady(player, new DevoutWitness());
    }

    private Permanent addReadyArtifact(Player player) {
        return addCreatureReady(player, new IronLance());
    }

    private Permanent addReadyEnchantment(Player player) {
        return addCreatureReady(player, new NoblePurpose());
    }
}
