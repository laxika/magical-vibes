package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreSpawn.class, GrizzlyBears.class, DoomBlade.class})
class PyreSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("When Pyre Spawn dies, it deals 3 damage to a chosen player")
    void deathTriggerDealsDamageToPlayer() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, spawn.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("When Pyre Spawn dies, it deals 3 damage to a chosen creature")
    void deathTriggerDealsDamageToCreature() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, spawn.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @CardUsed(SorinTheMirthless.class)
    @DisplayName("Death trigger can target a planeswalker and removes 3 loyalty")
    void deathTriggerDamagesPlaneswalker() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        Permanent sorin = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, spawn.getId());

        harness.handlePermanentChosen(player1, sorin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sorin);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The dying creature's controller chooses the target even when an opponent destroys it")
    void opponentControlsDeathTrigger() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player2, new PyreSpawn());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, spawn.getId());

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Pyre Spawn");
    }

    @Test
    @DisplayName("Death trigger marks exactly 3 damage on a creature that survives")
    void deathTriggerDamagesSurvivingCreature() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PyreSpawn());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, spawn.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Pyre Spawn");
    }

    @Test
    @DisplayName("Death trigger does not deal damage when its only target leaves before resolution")
    void removedTargetReceivesNoDamage() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, spawn.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
