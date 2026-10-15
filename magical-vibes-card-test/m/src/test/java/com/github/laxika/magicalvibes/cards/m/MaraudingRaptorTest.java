package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.CeruleanDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavingGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaraudingRaptor.class, ColossalDreadmaw.class, GrizzlyBears.class,
        HillGiant.class, CeruleanDrake.class, SavingGrace.class})
class MaraudingRaptorTest extends BaseCardTest {

    @Test
    void creatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void dinosaurEnteringIsDamagedAndBoostsMaraudingRaptor() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dreadmaw = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(4);
    }

    @Test
    void nonDinosaurEnteringIsDamagedWithoutBoostingMaraudingRaptor() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    void costReductionDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringRaptorDoesNotDamageItself() {
        harness.setHand(player1, List.of(new MaraudingRaptor()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Marauding Raptor").getMarkedDamage()).isZero();
    }

    @Test
    void opposingCreatureEntryDoesNotTrigger() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());

        Permanent opponentRaptor = harness.enterBattlefieldAndReturn(player2, new MaraudingRaptor());

        assertThat(gd.stack).isEmpty();
        assertThat(opponentRaptor.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    void protectionFromRedPreventsDamage() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());
        Permanent drake = harness.enterBattlefieldAndReturn(player1, new CeruleanDrake());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cerulean Drake");
        assertThat(drake.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    void dinosaurBoostsAccumulateAndExpireAtEndOfTurn() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.passBothPriorities();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    void costReductionDoesNotRemoveColoredManaRequirement() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new MaraudingRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageRedirectedToADinosaurStillBoostsRaptor() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, raptor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringRaptor = harness.enterBattlefieldAndReturn(player1, new MaraudingRaptor());
        harness.passBothPriorities();

        assertThat(enteringRaptor.getMarkedDamage()).isZero();
        assertThat(raptor.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(4);
    }
}
