package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarnessedLightning.class, EagerConstruct.class, Forest.class})
class HarnessedLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Gets three energy, then deals the chosen amount of damage")
    void getsEnergyAndDealsChosenDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        cast(target);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        PendingInteraction.XValueChoice choice = (PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.maxValue()).isEqualTo(3);
        assertThat(choice.manaPayment()).isFalse();

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertNotOnBattlefield(player2, "Eager Construct");
        harness.assertInGraveyard(player2, "Eager Construct");
    }

    @Test
    @DisplayName("Paying zero energy deals no damage")
    void paysZeroEnergy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        cast(target);

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HarnessedLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles without giving energy when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        harness.setHand(player1, List.of(new HarnessedLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can spend stored energy in addition to the three gained")
    void spendsStoredEnergy() {
        gd.setPlayerEnergyCounters(player1.getId(), 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        cast(target);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
        PendingInteraction.XValueChoice choice = (PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.maxValue()).isEqualTo(7);

        harness.handleXValueChosen(player1, 6);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player2, "Eager Construct");
        harness.assertInGraveyard(player1, "Harnessed Lightning");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partial payment deals exactly that much damage and keeps unused energy")
    void paysOnlyPartOfAvailableEnergy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerConstruct());
        cast(target);

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Harnessed Lightning");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay more energy than is available")
    void rejectsExcessiveEnergyPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        cast(target);

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 4))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleXValueChosen(player1, 0);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new HarnessedLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
