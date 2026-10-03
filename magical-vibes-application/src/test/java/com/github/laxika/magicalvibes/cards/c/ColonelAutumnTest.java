package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
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

@CardUsed({ColonelAutumn.class, CraigBooneNovacGuard.class, CathedralAcolyte.class, SwordsToPlowshares.class})
class ColonelAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("Its exploit trigger puts a +1/+1 counter on each creature you control")
    void exploitPutsCountersOnEachControlledCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());

        castColonel();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent colonel = findPermanent(player1, "Colonel Autumn");
        assertThat(colonel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cathedral Acolyte");
    }

    @Test
    @DisplayName("Other legendary creatures you control gain exploit")
    void grantsExploitToOtherLegendaryCreatures() {
        harness.addToBattlefield(player1, new ColonelAutumn());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());

        harness.castFromHand(player1, new CraigBooneNovacGuard(), "{1}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent craig = findPermanent(player1, "Craig Boone, Novac Guard");
        assertThat(craig.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Colonel Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonlegendary creatures do not gain exploit")
    void doesNotGrantExploitToNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new ColonelAutumn());

        harness.castFromHand(player1, new CathedralAcolyte(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Colonel Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining exploit neither sacrifices a creature nor puts counters on creatures")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());

        castColonel();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Cathedral Acolyte");
        assertThat(fodder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Colonel Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Colonel Autumn can exploit itself and still put counters on surviving creatures")
    void selfExploitTriggersCounters() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CathedralAcolyte());

        castColonel();
        Permanent colonel = findPermanent(player1, "Colonel Autumn");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, colonel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Colonel Autumn");
        harness.assertNotOnBattlefield(player1, "Colonel Autumn");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with granted exploit can sacrifice itself")
    void grantedExploitCanSacrificeItsSource() {
        Permanent colonel = harness.addToBattlefieldAndReturn(player1, new ColonelAutumn());
        harness.castFromHand(player1, new CraigBooneNovacGuard(), "{1}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent craig = findPermanent(player1, "Craig Boone, Novac Guard");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, craig.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Craig Boone, Novac Guard");
        assertThat(colonel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents' legendary creatures do not gain exploit")
    void doesNotGrantExploitToOpponentsLegendaryCreatures() {
        Permanent colonel = harness.addToBattlefieldAndReturn(player1, new ColonelAutumn());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CraigBooneNovacGuard(), "{1}{R}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craig Boone, Novac Guard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(colonel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing Colonel Autumn before exploit resolves allows sacrifice but gives no counters")
    void removedExploitSourceDoesNotTriggerCounters() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        harness.castFromHand(player1, new ColonelAutumn(), "{1}{W}{B}");
        harness.passBothPriorities();

        Permanent colonel = findPermanent(player1, "Colonel Autumn");
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, colonel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertNotOnBattlefield(player1, "Colonel Autumn");
        harness.assertInGraveyard(player1, "Cathedral Acolyte");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A granted exploit trigger remains on the stack after Colonel Autumn leaves")
    void grantedExploitStillResolvesAfterColonelLeaves() {
        Permanent colonel = harness.addToBattlefieldAndReturn(player1, new ColonelAutumn());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        harness.castFromHand(player1, new CraigBooneNovacGuard(), "{1}{R}{W}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, colonel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Cathedral Acolyte");
        assertThat(findPermanent(player1, "Craig Boone, Novac Guard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already-triggered counter ability resolves even after Colonel Autumn leaves")
    void counterTriggerStillResolvesAfterColonelLeaves() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CathedralAcolyte());
        castColonel();
        Permanent colonel = findPermanent(player1, "Colonel Autumn");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, colonel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Colonel Autumn");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colonel Autumn gains life when it deals combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        Permanent colonel = harness.addToBattlefieldAndReturn(player1, new ColonelAutumn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        colonel.setSummoningSick(false);
        colonel.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castColonel() {
        harness.castFromHand(player1, new ColonelAutumn(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
