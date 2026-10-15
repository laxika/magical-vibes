package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OstrichHorse;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BumiUnleashed.class, Forest.class, OstrichHorse.class})
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
        Permanent nonlandCreature = addCreatureReady(player1, new OstrichHorse());
        attacker.setSummoningSick(false);
        forest.tap();

        dealCombatDamage(attacker);

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.onlyLandCreaturesCanAttackThisCombat).isTrue();

        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(battlefieldIndex(nonlandCreature))))
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

    @Test
    @DisplayName("Only controlled lands untap, including lands that are not creatures")
    void untapsOnlyControlledLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent attacker = castBumiWithEarthbendedForest(forest);
        Permanent ordinaryLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonlandCreature = addCreatureReady(player1, new OstrichHorse());
        attacker.setSummoningSick(false);
        attacker.tap();
        forest.tap();
        ordinaryLand.tap();
        opposingLand.tap();
        nonlandCreature.tap();

        dealCombatDamage(attacker);

        assertThat(forest.isTapped()).isFalse();
        assertThat(ordinaryLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isTrue();
        assertThat(nonlandCreature.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The extra combat restriction applies immediately without a separate trigger")
    void restrictionAppliesAsExtraCombatBegins() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent attacker = castBumiWithEarthbendedForest(forest);
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.onlyLandCreaturesCanAttackThisCombat).isTrue();
        assertThat(als.canAttack(gd, attacker, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, forest, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("An earthbended land returns tapped as a new noncreature land after dying")
    void earthbendedLandReturnsTappedAfterDying() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumiWithEarthbendedForest(forest);
        forest.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(forest.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An earthbended land returns tapped after being exiled")
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumiWithEarthbendedForest(forest);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, forest));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(forest.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> resolveCombat());
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

}
