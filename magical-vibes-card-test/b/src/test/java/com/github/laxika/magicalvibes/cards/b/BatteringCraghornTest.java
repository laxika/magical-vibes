package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
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

@CardUsed({BatteringCraghorn.class, WirewoodElf.class})
class BatteringCraghornTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 1/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        Permanent craghorn = addCreatureReady(player1, new BatteringCraghorn());
        craghorn.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WirewoodElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Battering Craghorn");
        harness.assertInGraveyard(player2, "Wirewood Elf");
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new BatteringCraghorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent craghorn = findPermanent(player1, "Battering Craghorn");
        assertThat(craghorn.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int craghornIndex = gd.playerBattlefields.get(player1.getId()).indexOf(craghorn);
        harness.turnFaceUp(player1, craghornIndex);
        harness.passBothPriorities();

        assertThat(craghorn.isFaceDown()).isFalse();
    }

    @Test
    void faceDownCraghornDoesNotHaveFirstStrike() {
        harness.setHand(player1, List.of(new BatteringCraghorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = findPermanent(player1, "Battering Craghorn");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BatteringCraghorn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat();

        harness.assertInGraveyard(player1, "Battering Craghorn");
        harness.assertOnBattlefield(player2, "Battering Craghorn");
    }

    @Test
    void turningFaceUpRestoresFirstStrikeForCombat() {
        harness.setHand(player1, List.of(new BatteringCraghorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent craghorn = findPermanent(player1, "Battering Craghorn");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, 0);
        assertThat(craghorn.isFaceDown()).isFalse();

        craghorn.setSummoningSick(false);
        craghorn.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WirewoodElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Battering Craghorn");
        harness.assertInGraveyard(player2, "Wirewood Elf");
    }

    @Test
    void genericManaCannotPayTheRedMorphRequirement() {
        harness.setHand(player1, List.of(new BatteringCraghorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent craghorn = findPermanent(player1, "Battering Craghorn");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(craghorn.isFaceDown()).isTrue();
        harness.assertOnBattlefield(player1, "Battering Craghorn");
    }
}
