package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IriniSengir;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.cards.r.RevekaWizardSavant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaronSengir.class, MesaFalcon.class, IriniSengir.class, RevekaWizardSavant.class})
class BaronSengirTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +2/+2 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDies() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());
        Permanent blocker = addCreatureReady(player2, new MesaFalcon());

        baron.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mesa Falcon");
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(7);
    }

    @Test
    @DisplayName("No counter when the blocking creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());

        MesaFalcon toughBlocker = new MesaFalcon();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(8);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        baron.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Mesa Falcon");
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Gets a counter when a creature damaged in combat dies later that turn")
    void getsCounterWhenDamagedCreatureDiesLaterThatTurn() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());

        MesaFalcon damagedCard = new MesaFalcon();
        damagedCard.setPower(0);
        damagedCard.setToughness(7);
        Permanent damagedCreature = addCreatureReady(player2, damagedCard);

        baron.setAttacking(true);

        damagedCreature.setBlocking(true);
        damagedCreature.addBlockingTarget(0);

        resolveCombat();

        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(5);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reveka),
                0,
                null,
                damagedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mesa Falcon");
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();

        resolveAllTriggers();

        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability grants a regeneration shield to another Vampire")
    void regeneratesAnotherVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent vampire = addCreatureReady(player1, new IriniSengir());

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability can target an opponent's Vampire")
    void regeneratesOpponentVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent opponentVampire = addCreatureReady(player2, new IriniSengir());

        harness.activateAbility(player1, 0, null, opponentVampire.getId());
        harness.passBothPriorities();

        assertThat(opponentVampire.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves another Vampire from lethal damage")
    void regenerationPreventsLethalDamage() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());
        Permanent vampire = addCreatureReady(player1, new IriniSengir());
        addCreatureReady(player2, new RevekaWizardSavant());

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(baron.isTapped()).isTrue();
        assertThat(vampire.isTapped()).isFalse();

        harness.activateAbility(player2, 0, null, vampire.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Irini Sengir");
        harness.assertNotInGraveyard(player1, "Irini Sengir");
        assertThat(vampire.getRegenerationShield()).isZero();
        assertThat(vampire.getMarkedDamage()).isZero();
        assertThat(vampire.isTapped()).isTrue();
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Tap ability cannot target Baron Sengir itself")
    void cannotRegenerateItself() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, baron.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability cannot target a non-Vampire creature")
    void cannotRegenerateNonVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent nonVampire = addCreatureReady(player1, new MesaFalcon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonVampire.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
