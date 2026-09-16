package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DaruHealer.class)
class DaruHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage dealt to a targeted player")
    void preventsNextDamageToTargetPlayer() {
        addCreatureReady(player1, new DaruHealer());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new DaruHealer());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents the next damage dealt to a targeted creature")
    void preventsNextDamageToTargetCreature() {
        addCreatureReady(player1, new DaruHealer());
        Permanent attacker = addCreatureReady(player1, new DaruHealer());
        Permanent target = addCreatureReady(player2, new DaruHealer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(target),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat(player1);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents only the next 1 damage dealt to a targeted player")
    void preventsOnlyNextDamageToTargetPlayer() {
        addCreatureReady(player1, new DaruHealer());
        Permanent firstAttacker = addCreatureReady(player1, new DaruHealer());
        Permanent secondAttacker = addCreatureReady(player1, new DaruHealer());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int firstAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker);
        int secondAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker);
        declareAttackers(player1, List.of(firstAttackerIndex, secondAttackerIndex));
        resolveCombat(player1);

        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Unused prevention shield wears off at end of turn")
    void unusedPreventionShieldWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DaruHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new DaruHealer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent healer = findPermanent(player1, "Daru Healer");
        assertThat(healer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        int healerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(healer);
        harness.turnFaceUp(player1, healerIndex);
        harness.passBothPriorities();

        assertThat(healer.isFaceDown()).isFalse();
    }

}
