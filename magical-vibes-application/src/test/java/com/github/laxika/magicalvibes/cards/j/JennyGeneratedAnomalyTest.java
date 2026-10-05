package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.SonicScrewdriver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JennyGeneratedAnomaly.class, Forest.class, SonicScrewdriver.class, RayOfCommand.class})
class JennyGeneratedAnomalyTest extends BaseCardTest {

    @Test
    void doubleStrikeExploresTwiceWhenItDealsCombatDamage() {
        harness.setHand(player1, List.of());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
    }

    @Test
    void exploringANonlandPutsACounterOnJenny() {
        Card nonland = new SonicScrewdriver();
        harness.setLibrary(player1, List.of(nonland));
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(nonland.getId());
    }

    @Test
    void keepingANonlandOnTopExploresItAgainDuringRegularDamage() {
        Card nonland = new SonicScrewdriver();
        harness.setLibrary(player1, List.of(nonland));
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(nonland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).doesNotContain(nonland.getId());
        harness.assertLife(player2, 15);
    }

    @Test
    void emptyLibraryStillGivesACounterForEachExplore() {
        harness.setLibrary(player1, List.of());
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 15);
    }

    @Test
    void changingControllerBeforeExploreResolvesUsesTheNewControllersLibrary() {
        Card originalControllersLand = new Forest();
        Card newControllersLand = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(newControllersLand));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            harness.castAndResolveInstant(player2, 0, jenny.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(jenny);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId).contains(newControllersLand.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(originalControllersLand.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
