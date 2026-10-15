package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
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

@CardUsed({AetherMeltdown.class, GrizzlyBears.class, SleekSchooner.class, FountainOfYouth.class})
class AetherMeltdownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature, gives two energy counters, and weakens it")
    void entersAndWeakensCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castAetherMeltdown(bears);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(findPermanent(player1, "Aether Meltdown").getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Can enchant a noncreature Vehicle")
    void canEnchantVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());

        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, schooner.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature non-Vehicle permanent")
    void cannotEnchantOtherPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("The weakening ends when Aether Meltdown leaves the battlefield")
    void weakeningEndsWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castAetherMeltdown(bears);

        Permanent aura = findPermanent(player1, "Aether Meltdown");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        castAetherMeltdown(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The energy trigger resolves separately and survives the Aura leaving")
    void energyTriggerSurvivesAuraLeaving() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Aether Meltdown"));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Vehicle enchanted before crewing is weakened when it becomes a creature")
    void weakensVehicleWhenCrewed() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());
        addCreatureReady(player2, new GrizzlyBears());
        castAetherMeltdown(schooner);

        assertThat(findPermanent(player1, "Aether Meltdown").getAttachedTo()).isEqualTo(schooner.getId());
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(3);
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents entry and energy gain")
    void missingTargetPreventsEnergyGain() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aether Meltdown");
        harness.assertInGraveyard(player1, "Aether Meltdown");
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    private void castAetherMeltdown(Permanent target) {
        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
