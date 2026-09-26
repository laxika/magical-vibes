package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.s.SokenzanBruiser;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YamabushisStorm.class, DevotedRetainer.class, SokenzanBruiser.class,
        PaladinEnVec.class, Bandage.class, Mountain.class})
class YamabushisStormTest extends BaseCardTest {

    private void castStorm() {
        harness.setHand(player1, List.of(new YamabushisStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Creatures killed by the damage are exiled instead of going to the graveyard")
    void killedCreaturesAreExiled() {
        harness.addToBattlefield(player1, new DevotedRetainer());
        harness.addToBattlefield(player2, new DevotedRetainer());

        castStorm();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Devoted Retainer");
        harness.assertNotOnBattlefield(player2, "Devoted Retainer");
        harness.assertNotInGraveyard(player1, "Devoted Retainer");
        harness.assertNotInGraveyard(player2, "Devoted Retainer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Devoted Retainer"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Devoted Retainer"));
    }

    @Test
    @DisplayName("Creatures that survive the damage stay on the battlefield and are not exiled")
    void survivingCreaturesRemain() {
        harness.addToBattlefield(player2, new SokenzanBruiser());

        castStorm();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Sokenzan Bruiser");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Sokenzan Bruiser"));
    }

    @Test
    @DisplayName("Noncreature permanents are not dealt damage or exiled")
    void noncreaturePermanentsAreUntouched() {
        harness.addToBattlefield(player2, new Mountain());

        castStorm();

        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("A creature with protection from red is not dealt damage")
    void protectedCreatureIsUntouched() {
        harness.addToBattlefield(player2, new PaladinEnVec());

        castStorm();

        harness.assertOnBattlefield(player2, "Paladin en-Vec");
    }

    @Test
    @DisplayName("A creature dealt damage by Storm that dies later this turn is exiled")
    void laterDeathsAreExiled() {
        Permanent target = addCreatureReady(player2, new SokenzanBruiser());
        castStorm();
        Permanent attacker = addCreatureReady(player1, new SokenzanBruiser());

        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Sokenzan Bruiser");
        harness.assertNotInGraveyard(player2, "Sokenzan Bruiser");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Sokenzan Bruiser"));
    }

    @Test
    @DisplayName("A creature whose Storm damage is prevented is not marked for later exile")
    void preventedDamageDoesNotMarkCreatureForExile() {
        Permanent target = addCreatureReady(player2, new DevotedRetainer());
        Permanent attacker = addCreatureReady(player1, new SokenzanBruiser());
        harness.setHand(player1, List.of(new Bandage()));
        harness.setLibrary(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        castStorm();
        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();

        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Devoted Retainer");
        harness.assertInGraveyard(player2, "Devoted Retainer");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Devoted Retainer"));
    }

    @Test
    @DisplayName("Players are not dealt damage")
    void playersAreNotDamaged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castStorm();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
