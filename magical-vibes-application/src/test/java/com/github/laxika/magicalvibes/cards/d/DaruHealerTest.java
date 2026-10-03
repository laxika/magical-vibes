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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
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

    @Test
    @DisplayName("Activating prevention taps the healer and prevents another activation")
    void tapCostPreventsRepeatedActivation() {
        Permanent healer = addCreatureReady(player1, new DaruHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(healer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A summoning-sick healer cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent healer = addCreatureReady(player1, new DaruHealer());
        healer.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(healer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two healers' shields combine to prevent two damage")
    void preventionShieldsCombine() {
        addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(2, 3, 4));
        resolveCombat(player1);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The prevention ability is unavailable face down and returns when turned face up")
    void morphHidesAndRestoresPreventionAbility() {
        harness.setHand(player1, List.of(new DaruHealer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent healer = findPermanent(player1, "Daru Healer");
        healer.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, 0);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new DaruHealer());
        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

}
