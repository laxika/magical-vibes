package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurtiveHomunculus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TheMasterMesmerist.class, GrizzlyBears.class, AirElemental.class, FurtiveHomunculus.class, Forest.class, Unsummon.class})
class TheMasterMesmeristTest extends BaseCardTest {

    @Test
    @DisplayName("The tap ability grants skulk and goads a legal opposing creature")
    void tapAbilityGrantsSkulkAndGoadsTarget() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(master.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The tap ability rejects creatures above the Master's power")
    void tapAbilityRejectsCreatureWithGreaterPower() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with skulk dealing combat damage puts a counter on the Master and draws")
    void skulkCreatureCombatDamagePutsCounterAndDraws() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player1, new FurtiveHomunculus());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage from a creature without skulk does not trigger")
    void nonSkulkCreatureCombatDamageDoesNotTrigger() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void tapAbilityRejectsOwnCreature() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapAbilityAcceptsEqualPower() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void targetBecomingTooPowerfulMakesBothEffectsFail() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    void usesMastersLastBattlefieldPowerAfterItIsReturnedToHand() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        master.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player1, 0, master.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Master, Mesmerist");
        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void skulkExpiresAtEndOfTurnButGoadLastsUntilNextTurn() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    void eachSkulkAttackerTriggersSeparately() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent first = addCreatureReady(player1, new FurtiveHomunculus());
        Permanent second = addCreatureReady(player1, new FurtiveHomunculus());
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void skulkCreatureDamagingMastersControllerDoesNotTrigger() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player2, new FurtiveHomunculus());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void stillDrawsIfMasterLeavesBeforeCombatDamageTriggerResolves() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent attacker = addCreatureReady(player1, new FurtiveHomunculus());
        attacker.setAttacking(true);

        resolveCombat();
        harness.castInstant(player1, 0, master.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Master, Mesmerist");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("The Master, Mesmerist", "Forest");
    }
}
