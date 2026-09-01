package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BumiUnleashed.class, Forest.class, GrizzlyBears.class})
class BumiUnleashedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void entersAndEarthbendsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BumiUnleashed()));
        addBumiMana();

        harness.castCreature(player1, 0, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage untaps lands and creates a land-creature-only extra combat")
    void combatDamageUntapsLandsAndCreatesRestrictedExtraCombat() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent attacker = castBumiWithEarthbendedForest(forest);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        forest.tap();

        dealCombatDamage(attacker);

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.onlyLandCreaturesCanAttackThisCombat).isTrue();

        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(battlefieldIndex(bear))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player1, List.of(battlefieldIndex(forest)));
    }

    @Test
    @DisplayName("The restriction ends with Bumi's extra combat")
    void restrictionEndsWithExtraCombat() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent attacker = castBumiWithEarthbendedForest(forest);
        attacker.setSummoningSick(false);
        dealCombatDamage(attacker);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.onlyLandCreaturesCanAttackThisCombat).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(als.canAttack(gd, forest, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, attacker, player1.getId())).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent castBumiWithEarthbendedForest(Permanent forest) {
        harness.setHand(player1, List.of(new BumiUnleashed()));
        addBumiMana();
        harness.castCreature(player1, 0, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Bumi, Unleashed");
    }

    private void addBumiMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void dealCombatDamage(Permanent attacker) {
        harness.forceActivePlayer(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

}
