package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetingImage.class, GrizzlyBears.class})
class FleetingImageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1}{U} ability puts return-to-hand on the stack")
    void activateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new FleetingImage());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating the ability requires one blue mana")
    void activateAbilityRequiresBlueMana() {
        harness.addToBattlefield(player1, new FleetingImage());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability requires one generic mana in addition to blue")
    void activateAbilityRequiresGenericMana() {
        harness.addToBattlefield(player1, new FleetingImage());

        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability can be activated while Fleeting Image is tapped")
    void abilityDoesNotRequireUntappedSource() {
        Permanent image = harness.addToBattlefieldAndReturn(player1, new FleetingImage());
        image.tap();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fleeting Image");
    }

    @Test
    @DisplayName("Activating {1}{U} ability returns Fleeting Image to owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new FleetingImage());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fleeting Image");
        harness.assertNotOnBattlefield(player1, "Fleeting Image");
    }

    @Test
    @DisplayName("Activating a controlled Fleeting Image returns it to its owner's hand")
    void activateAbilityReturnsToOwnersHandWhenControlledByOpponent() {
        FleetingImage image = new FleetingImage();
        image.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, image);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fleeting Image");
        harness.assertNotInHand(player2, "Fleeting Image");
        harness.assertNotOnBattlefield(player2, "Fleeting Image");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Fleeting Image")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new FleetingImage());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("The ability does nothing if Fleeting Image leaves before resolution")
    void abilityDoesNothingIfSourceLeavesBeforeResolution() {
        FleetingImage image = new FleetingImage();
        harness.addToBattlefield(player1, image);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(image.getId()));
    }

    @Test
    @DisplayName("Multiple activations return only their source, once")
    void multipleActivationsDoNotReturnAnotherImage() {
        FleetingImage source = new FleetingImage();
        harness.addToBattlefield(player1, source);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FleetingImage());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getId().equals(source.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("A flying creature can block Fleeting Image")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new FleetingImage());
        addCreatureReady(player2, new FleetingImage());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.assertInGraveyard(player1, "Fleeting Image");
        harness.assertInGraveyard(player2, "Fleeting Image");
    }
}
