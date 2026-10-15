package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BaronSengir;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.f.FalkenrathCelebrants;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VampireSlayer.class, BaronSengir.class, GiantSpider.class, RabidBite.class, FalkenrathCelebrants.class, SporebackWolf.class})
class VampireSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Vampire it deals damage to")
    void destroysDamagedVampire() {
        harness.addToBattlefield(player1, new VampireSlayer());
        harness.addToBattlefield(player2, new BaronSengir());
        castRabidBite("Baron Sengir");

        harness.assertInGraveyard(player2, "Baron Sengir");
    }

    @Test
    @DisplayName("Does not destroy a non-Vampire it deals damage to")
    void doesNotDestroyNonVampire() {
        harness.addToBattlefield(player1, new VampireSlayer());
        harness.addToBattlefield(player2, new GiantSpider());
        castRabidBite("Giant Spider");

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Destroys a Vampire damaged in combat even when Vampire Slayer dies")
    void destroysVampireAfterDyingInCombat() {
        Permanent slayer = addCreatureReady(player1, new VampireSlayer());
        slayer.setAttacking(true);
        addCreatureReady(player2, new FalkenrathCelebrants());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();

        harness.assertInGraveyard(player1, "Vampire Slayer");
        harness.assertOnBattlefield(player2, "Falkenrath Celebrants");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Falkenrath Celebrants");
    }

    @Test
    @DisplayName("Another creature's damage to a Vampire does not trigger Vampire Slayer")
    void anotherCreatureDamagingVampireDoesNotTrigger() {
        addCreatureReady(player1, new VampireSlayer());
        Permanent wolf = addCreatureReady(player1, new SporebackWolf());
        wolf.setAttacking(true);
        addCreatureReady(player2, new FalkenrathCelebrants());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Falkenrath Celebrants");
        harness.assertOnBattlefield(player1, "Vampire Slayer");
    }

    private void castRabidBite(String targetName) {
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Vampire Slayer"),
                harness.getPermanentId(player2, targetName)));
        resolveAllTriggers();
    }
}
