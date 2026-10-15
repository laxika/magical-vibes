package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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

@CardUsed({PestilentSouleater.class})
class PestilentSouleaterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating infect ability puts it on the stack")
    void activatingInfectPutsOnStack() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Resolving infect ability grants infect until end of turn")
    void resolvingInfectAbilityGrantsInfect() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect granted by ability resets at end of turn cleanup")
    void infectResetsAtEndOfTurn() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no black mana available")
    void paysLifeWhenNoBlackMana() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers black mana over life payment when available")
    void prefersBlackManaOverLife() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability does NOT tap Pestilent Souleater")
    void activatingAbilityDoesNotTap() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(souleater.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent souleater = addCreatureReady(player1, new PestilentSouleater());
        souleater.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability resolves without effect if Pestilent Souleater is removed before resolution")
    void abilityResolvesWithoutEffectIfSourceRemoved() {
        addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedActivationsDoNotMultiplyPoisonDamage() {
        addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    void infectDamageToCreatureUsesCountersInsteadOfMarkedDamage() {
        addCreatureReady(player1, new PestilentSouleater());
        Permanent blocker = addCreatureReady(player2, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Pestilent Souleater");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void cannotPayLifeWithOnlyOneLifeAndNoBlackMana() {
        addCreatureReady(player1, new PestilentSouleater());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pendingAbilityDoesNotGrantInfectToReturnedSource() {
        Permanent original = addCreatureReady(player1, new PestilentSouleater());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.INFECT)).isFalse();
    }
}
