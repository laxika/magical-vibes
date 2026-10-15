package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DugganPrivateDetective.class, GrizzlyBears.class, DinosaursOnASpaceship.class})
class DugganPrivateDetectiveTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness update as the controller's hand changes")
    void handSizeUpdatesContinuously() {
        Permanent duggan = addCreatureReady(player1, new DugganPrivateDetective());
        harness.setHand(player1, List.of(new DugganPrivateDetective()));
        harness.setHand(player2, List.of(new DugganPrivateDetective(), new DugganPrivateDetective()));

        assertThat(gqs.getEffectivePower(gd, duggan)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duggan)).isEqualTo(1);

        harness.setHand(player1, List.of(new DugganPrivateDetective(), new DugganPrivateDetective(),
                new DugganPrivateDetective()));

        assertThat(gqs.getEffectivePower(gd, duggan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duggan)).isEqualTo(3);
    }

    @Test
    @DisplayName("The punch uses twice Duggan's power at resolution")
    void punchUsesPowerAtResolution() {
        addCreatureReady(player1, new DugganPrivateDetective());
        Permanent target = addCreatureReady(player2, new DinosaursOnASpaceship());
        harness.setHand(player1, List.of(new DugganPrivateDetective()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.setHand(player1, List.of(new DugganPrivateDetective(), new DugganPrivateDetective(),
                new DugganPrivateDetective()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dinosaurs on a Spaceship");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("The punch may target another creature its controller controls")
    void punchCanTargetOwnCreature() {
        addCreatureReady(player1, new DugganPrivateDetective());
        Permanent target = addCreatureReady(player1, new DinosaursOnASpaceship());
        harness.setHand(player1, List.of(new DugganPrivateDetective(), new DugganPrivateDetective()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dinosaurs on a Spaceship");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering with an empty hand still investigates after Duggan dies")
    void entryTriggerSurvivesZeroToughnessDeath() {
        harness.castFromHand(player1, new DugganPrivateDetective(), "{2}{G}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Duggan, Private Detective");
        harness.assertInGraveyard(player1, "Duggan, Private Detective");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Power and toughness equal the controller's hand size")
    void powerAndToughnessEqualHandSize() {
        Permanent duggan = addCreatureReady(player1, new DugganPrivateDetective());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, duggan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duggan)).isEqualTo(3);
    }

    @Test
    @DisplayName("Investigates when it enters the battlefield")
    void investigatesOnEntry() {
        harness.setHand(player1, List.of(new DugganPrivateDetective(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates when it attacks")
    void investigatesOnAttack() {
        addCreatureReady(player1, new DugganPrivateDetective());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Deals twice its power to another target creature")
    void dealsTwiceItsPowerToAnotherCreature() {
        Permanent duggan = addCreatureReady(player1, new DugganPrivateDetective());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(duggan.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target itself or activate the ability twice")
    void targetAndActivationRestrictions() {
        Permanent duggan = addCreatureReady(player1, new DugganPrivateDetective());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, duggan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        duggan.untap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
