package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctuaryLockdown.class, EliteVanguard.class, GrizzlyBears.class,
        Opalescence.class, MaskwoodNexus.class})
class SanctuaryLockdownTest extends BaseCardTest {

    @Test
    @DisplayName("Humans you control get +1/+1")
    void buffsHumansYouControl() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHuman = addCreatureReady(player2, new EliteVanguard());
        addCreatureReady(player1, new SanctuaryLockdown());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHuman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping two Humans taps an opponent's target creature")
    void tapsTwoHumansAndOpponentCreature() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());
        harness.passBothPriorities();

        assertThat(human1.isTapped()).isTrue();
        assertThat(human2.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(lockdown.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lockdown),
                null,
                ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(human1.isTapped()).isFalse();
        assertThat(human2.isTapped()).isFalse();
    }

    @Test
    void summoningSickHumansCanPayTheTapCost() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent human2 = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        human1.setSummoningSick(true);
        human2.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());

        assertThat(human1.isTapped()).isTrue();
        assertThat(human2.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void choosesOnlyTwoOfThreeHumans() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent human3 = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());
        harness.handlePermanentChosen(player1, human2.getId());
        harness.handlePermanentChosen(player1, human3.getId());

        assertThat(human1.isTapped()).isFalse();
        assertThat(human2.isTapped()).isTrue();
        assertThat(human3.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void nonHumansAndOpponentsHumansCannotCompleteTheCost() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHuman = addCreatureReady(player2, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, opponentHuman.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(human.isTapped()).isFalse();
        assertThat(nonHuman.isTapped()).isFalse();
        assertThat(opponentHuman.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyTappedHumanCannotPayTheCost() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        human2.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(human1.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotTapHumans() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(human1.isTapped()).isFalse();
        assertThat(human2.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterLockdownLeavesAndItsBoostEnds() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lockdown);
        gd.playerGraveyards.get(player1.getId()).add(lockdown.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(human1.isTapped()).isTrue();
        assertThat(human2.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, human1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human1)).isEqualTo(1);
    }

    @Test
    void targetThatBecomesControlledByYouIsNotTapped() {
        Permanent lockdown = addCreatureReady(player1, new SanctuaryLockdown());
        Permanent human1 = addCreatureReady(player1, new EliteVanguard());
        Permanent human2 = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(human1.isTapped()).isTrue();
        assertThat(human2.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SanctuaryLockdown.class, Opalescence.class, MaskwoodNexus.class})
    void animatedHumanLockdownReceivesItsOwnBoost() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent lockdown = harness.addToBattlefieldAndReturn(player1, new SanctuaryLockdown());

        assertThat(gqs.getEffectivePower(gd, lockdown)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lockdown)).isEqualTo(4);
    }
}
