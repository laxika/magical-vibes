package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
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

@CardUsed({CuriousHomunculus.class, Divination.class, GrizzlyBears.class, Shock.class, Terrarion.class, Unsubstantiate.class})
class CuriousHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms during its controller's upkeep with three instant or sorcery cards in the graveyard")
    void transformsWithThreeInstantOrSorceryCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(homunculus.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform without three instant or sorcery cards in the graveyard")
    void doesNotTransformWithoutThreshold() {
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(homunculus.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Restricted mana casts instant and sorcery spells but not creature spells")
    void restrictedManaIsInstantSorceryOnly() {
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        homunculus.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Voracious Reader reduces instant and sorcery costs and has prowess")
    void backFaceReducesSpellCostsAndHasProwess() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());

        transform(homunculus);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);
        int toughnessBeforeCast = gqs.getEffectiveToughness(gd, homunculus);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast + 1);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(toughnessBeforeCast + 1);
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(homunculus.isTransformed()).isFalse();
    }

    @Test
    void opponentsGraveyardDoesNotCountTowardThreshold() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.setGraveyard(player2, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(homunculus.isTransformed()).isFalse();
    }

    @Test
    void thresholdIsCheckedAgainWhenTriggerResolves() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.passBothPriorities();

        assertThat(homunculus.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSicknessPreventsManaAbility() {
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        homunculus.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(homunculus.isTapped()).isFalse();
    }

    @Test
    void costReductionDoesNotRemoveColoredManaRequirements() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        transform(homunculus);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast + 1);
    }

    @Test
    void creatureSpellsAreNotDiscountedAndDoNotTriggerProwess() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        transform(homunculus);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast);
    }

    @Test
    void restrictedManaPaysForInstantButNotArtifact() {
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        homunculus.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(homunculus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new Terrarion()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, homunculus.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Curious Homunculus");
        harness.assertInHand(player1, "Curious Homunculus");
    }

    @Test
    void backFaceDiscountsInstantSpells() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        transform(homunculus);
        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);

        harness.castInstant(player1, 0, homunculus.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast + 1);
    }

    @Test
    void artifactSpellTriggersProwessWithoutCostReductionAndBonusExpires() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        transform(homunculus);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Terrarion()));
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);
        int toughnessBeforeCast = gqs.getEffectiveToughness(gd, homunculus);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast + 1);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(toughnessBeforeCast + 1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(toughnessBeforeCast);
        assertThat(homunculus.isTransformed()).isTrue();
    }

    @Test
    void opponentsSpellReceivesNoDiscountAndDoesNotTriggerProwess() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, new CuriousHomunculus());
        transform(homunculus);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsubstantiate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, homunculus.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, homunculus.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast);
    }

    private void transform(Permanent homunculus) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(homunculus.isTransformed()).isTrue();
    }
}
