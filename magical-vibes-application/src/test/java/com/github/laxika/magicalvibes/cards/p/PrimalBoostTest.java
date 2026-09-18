package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.w.WirewoodLodge;
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

@CardUsed({PrimalBoost.class, ElvishWarrior.class, WirewoodLodge.class})
class PrimalBoostTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +4/+4")
    void boostsTargetCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new PrimalBoost()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        assertThat(warrior.getPowerModifier()).isEqualTo(4);
        assertThat(warrior.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cycling optionally boosts a creature and draws a card")
    void cyclingBoostsCreatureAndDraws() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        prepareCycle();
        cycleAndChoose(warrior, true);

        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Primal Boost");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cycling may decline the creature boost and still draws a card")
    void cyclingMayBeDeclined() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        prepareCycle();
        cycleAndChoose(warrior, false);

        assertThat(warrior.getPowerModifier()).isZero();
        assertThat(warrior.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Primal Boost");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cycling without a target still draws a card")
    void cyclingWithoutTargetDraws() {
        harness.setHand(player1, List.of(new PrimalBoost()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Primal Boost");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        harness.setHand(player1, List.of(new PrimalBoost()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void prepareCycle() {
        harness.setHand(player1, List.of(new PrimalBoost()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        addCyclingMana();
    }

    private void cycleAndChoose(Permanent target, boolean accept) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
