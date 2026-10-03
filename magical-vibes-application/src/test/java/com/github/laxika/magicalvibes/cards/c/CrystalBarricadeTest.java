package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalBarricade.class, GrizzlyBears.class, Shock.class})
class CrystalBarricadeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot target the controller")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Noncombat damage to another creature you control is prevented")
    void preventsNoncombatDamageToAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The source creature is not protected by its own effect")
    void doesNotPreventNoncombatDamageToSource() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, barricade.getId());

        assertThat(barricade.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to another creature you control is not prevented")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        Permanent blocker = addReadyBlocker(player1);
        addReadyAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void controllerCanTargetThemself() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void opposingCreaturesAreNotProtected() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void twoBarricadesProtectEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());
        harness.addToBattlefield(player1, new CrystalBarricade());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, first.getId());

        assertThat(first.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void losingAbilitiesEndsPlayerHexproof() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, barricade.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void losingAbilitiesEndsCreatureDamagePrevention() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, barricade.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void preventionEndsWhenBarricadeDies() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, barricade.getId());
        harness.castAndResolveInstant(player1, 0, barricade.getId());
        harness.assertInGraveyard(player1, "Crystal Barricade");
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addReadyBlocker(Player player) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }
}
