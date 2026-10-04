package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DubiousDelicacy.class, AirElemental.class})
class DubiousDelicacyTest extends BaseCardTest {

    @Test
    void entersAndGivesTargetCreatureMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void mayEnterWithoutTarget() {
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dubious Delicacy");
    }

    @Test
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new DubiousDelicacy());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Dubious Delicacy");
    }

    @Test
    void sacrificingItMakesTargetOpponentLoseThreeLife() {
        harness.addToBattlefield(player1, new DubiousDelicacy());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Dubious Delicacy");
    }

    @Test
    void canTargetOwnCreatureAndModifierExpiresAfterTheTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void mayChooseNoTargetEvenWhenACreatureIsAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dubious Delicacy");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void sacrificeIsPaidBeforeLifeGainResolves() {
        harness.addToBattlefield(player1, new DubiousDelicacy());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Dubious Delicacy");
        harness.assertInGraveyard(player1, "Dubious Delicacy");
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    void lifeLossAbilityCannotTargetItsController() {
        harness.addToBattlefield(player1, new DubiousDelicacy());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dubious Delicacy");
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedArtifactCannotPayEitherTapCost() {
        Permanent delicacy = harness.addToBattlefieldAndReturn(player1, new DubiousDelicacy());
        delicacy.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dubious Delicacy");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dubious Delicacy");
    }

    @Test
    void enterTriggerStillResolvesAfterArtifactIsSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DubiousDelicacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dubious Delicacy");
        harness.assertLife(player2, 17);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }
}
