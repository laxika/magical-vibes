package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.e.ExpendableTroops;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefenderOfChaos.class, Cessation.class, ExpendableTroops.class})
class DefenderOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Defender of Chaos to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.setHand(player1, List.of(new DefenderOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Defender of Chaos");
    }

    @Test
    @DisplayName("Protection from white prevents a white Aura spell from targeting Defender of Chaos")
    void protectionFromWhitePreventsWhiteAuraTargeting() {
        Permanent defender = addCreatureReady(player1, new DefenderOfChaos());

        harness.setHand(player2, List.of(new Cessation()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, defender.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white prevents abilities of white sources from targeting Defender of Chaos")
    void protectionFromWhitePreventsWhiteAbilityTargeting() {
        Permanent defender = addCreatureReady(player1, new DefenderOfChaos());
        addCreatureReady(player2, new ExpendableTroops());
        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, defender.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white prevents white creatures from blocking Defender of Chaos")
    void protectionFromWhitePreventsWhiteBlocking() {
        addCreatureReady(player1, new DefenderOfChaos());
        addCreatureReady(player2, new ExpendableTroops());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents combat damage from white creatures")
    void protectionFromWhitePreventsCombatDamage() {
        Permanent defender = addCreatureReady(player1, new DefenderOfChaos());
        addCreatureReady(player2, new ExpendableTroops());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        assertThat(defender.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Defender of Chaos");
        harness.assertInGraveyard(player2, "Expendable Troops");
    }
}
