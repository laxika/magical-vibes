package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BilboFellowConspirator;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.d.DwarvenWarriors;
import com.github.laxika.magicalvibes.cards.i.InvasionOfAlara;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThorinCompanysLeader.class, BilboFellowConspirator.class, DwarvenWarriors.class,
        InvasionOfAlara.class, ChandraHopesBeacon.class})
class ThorinCompanysLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("A Dwarf dealing combat damage creates two Treasures")
    void dwarfCombatDamageCreatesTwoTreasures() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent dwarf = addCreatureReady(player1, new DwarvenWarriors());
        dwarf.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Non-Dwarf combat damage does not create Treasures")
    void nonDwarfCombatDamageDoesNotCreateTreasures() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent bilbo = addCreatureReady(player1, new BilboFellowConspirator());
        bilbo.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability grants own creatures double strike until end of turn")
    void activatedAbilityGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent bilbo = addCreatureReady(player1, new BilboFellowConspirator());
        Permanent opponentBilbo = addCreatureReady(player2, new BilboFellowConspirator());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bilbo, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBilbo, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bilbo, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void thorinOwnCombatDamageCreatesTreasures() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        thorin.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
    }

    @Test
    void eachDwarfCreatesItsOwnTwoTreasures() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent dwarf = addCreatureReady(player1, new DwarvenWarriors());
        thorin.setAttacking(true);
        dwarf.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    void opposingDwarfDoesNotTriggerThorin() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent dwarf = addCreatureReady(player2, new DwarvenWarriors());
        dwarf.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void combatDamageToCreatureDoesNotCreateTreasures() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent blocker = addCreatureReady(player2, new BilboFellowConspirator());
        thorin.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Bilbo, Fellow Conspirator");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void combatDamageToBattleCreatesTreasures() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfAlara());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 7);
        thorin.setAttacking(true);
        thorin.setAttackTarget(battle.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void combatDamageToPlaneswalkerDoesNotCreateTreasures() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        thorin.setAttacking(true);
        thorin.setAttackTarget(chandra.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void doubleStrikeCreatesTreasuresInBothDamageSteps() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        thorin.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertLife(player2, 12);
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    void grantOnlyAffectsCreaturesPresentAtResolution() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new DwarvenWarriors());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new BilboFellowConspirator());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void abilityResolvesAfterThorinLeavesBattlefield() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent dwarf = addCreatureReady(player1, new DwarvenWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(thorin);
        gd.playerGraveyards.get(player1.getId()).add(thorin.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void nineManaCannotActivateAbility() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void tappedSummoningSickThorinCanActivateWithColoredMana() {
        Permanent thorin = harness.addToBattlefieldAndReturn(player1, new ThorinCompanysLeader());
        thorin.setSummoningSick(true);
        thorin.tap();
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(thorin.isTapped()).isTrue();
    }
}
