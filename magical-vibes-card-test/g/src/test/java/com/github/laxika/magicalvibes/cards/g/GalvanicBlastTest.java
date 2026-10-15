package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.f.FlamebornHellion;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalvanicBlast.class, GrizzlyBears.class, Spellbook.class, LeoninScimitar.class,
        Memnite.class, FlamebornHellion.class, KothOfTheHammer.class})
class GalvanicBlastTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Deals 2 damage to target player without metalcraft")
    void deals2DamageToPlayerWithoutMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature without metalcraft")
    void deals2DamageToCreatureWithoutMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // 2 damage kills a 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Deals 4 damage to target player with metalcraft")
    void deals4DamageToPlayerWithMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature with metalcraft")
    void deals4DamageToCreatureWithMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("Deals only 2 damage if metalcraft lost before resolution")
    void deals2DamageIfMetalcraftLostBeforeResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, player2.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Should deal only 2 (base) since metalcraft is no longer met
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void deals4DamageIfMetalcraftGainedBeforeResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Memnite());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void opponentsArtifactsDoNotCountTowardsMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void killsFourToughnessCreatureWithMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }
        var target = harness.addToBattlefieldAndReturn(player2, new FlamebornHellion());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flameborn Hellion");
        harness.assertInGraveyard(player2, "Flameborn Hellion");
    }

    @Test
    void deals2DamageToPlaneswalkerWithoutMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        var target = harness.enterBattlefieldAndReturn(player2, new KothOfTheHammer());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Koth of the Hammer");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void killsPlaneswalkerWithMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }
        var target = harness.enterBattlefieldAndReturn(player2, new KothOfTheHammer());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Koth of the Hammer");
        harness.assertInGraveyard(player2, "Koth of the Hammer");
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private void addThreeArtifacts(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }
}
