package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirsdagCultist.class, AvacynsPilgrim.class, LilianaOfTheVeil.class})
class SkirsdagCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with only self as creature auto-sacrifices self and puts ability on stack")
    void autoSacrificesSelfAsOnlyCreature() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Auto-sacrificed the only creature (itself)
        harness.assertNotOnBattlefield(player1, "Skirsdag Cultist");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple creatures asks to choose which to sacrifice")
    void asksForChoiceWithMultipleCreatures() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addToBattlefield(player1, new AvacynsPilgrim());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a creature to sacrifice puts ability on stack")
    void choosingCreaturePutsAbilityOnStack() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addToBattlefield(player1, new AvacynsPilgrim());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID pilgrimId = findPermanent(player1, "Avacyn's Pilgrim").getId();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, pilgrimId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
        // Cultist should still be on battlefield
        harness.assertOnBattlefield(player1, "Skirsdag Cultist");
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addToBattlefield(player2, new AvacynsPilgrim());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID pilgrimId = findPermanent(player2, "Avacyn's Pilgrim").getId();

        harness.activateAbility(player1, 0, null, pilgrimId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Cannot activate ability after its source leaves the battlefield")
    void cannotActivateAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        // The source no longer exists on the battlefield.
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new SkirsdagCultist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the costs taps the Cultist and sacrifices a tapped creature before damage resolves")
    void paysCostsBeforeResolution() {
        Permanent cultist = addCreatureReady(player1, new SkirsdagCultist());
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        pilgrim.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, pilgrim.getId());

        assertThat(cultist.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped Cultist cannot pay its tap cost")
    void cannotActivateWhenTapped() {
        Permanent cultist = addCreatureReady(player1, new SkirsdagCultist());
        cultist.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Skirsdag Cultist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability may target its controller")
    void canDamageController() {
        addCreatureReady(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Skirsdag Cultist");
    }

    @Test
    @DisplayName("The targeted creature may also be sacrificed to pay the cost")
    void sacrificedTargetMakesAbilityFailToResolve() {
        addCreatureReady(player1, new SkirsdagCultist());
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, pilgrim.getId());
        harness.handlePermanentChosen(player1, pilgrim.getId());

        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skirsdag Cultist");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Cultist can target itself and sacrifice another creature")
    void canTargetSelfWhileSacrificingAnotherCreature() {
        Permanent cultist = addCreatureReady(player1, new SkirsdagCultist());
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, cultist.getId());
        harness.handlePermanentChosen(player1, pilgrim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertInGraveyard(player1, "Skirsdag Cultist");
    }

    @Test
    @DisplayName("The ability removes two loyalty counters from a targeted planeswalker")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new SkirsdagCultist());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, liliana.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Liliana of the Veil");
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }
}
