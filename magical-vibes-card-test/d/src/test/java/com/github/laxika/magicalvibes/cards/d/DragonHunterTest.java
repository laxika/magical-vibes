package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TerritorialRoc;
import com.github.laxika.magicalvibes.cards.t.ThunderbreakRegent;
import com.github.laxika.magicalvibes.cards.t.TwinBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonHunter.class, ThunderbreakRegent.class, TerritorialRoc.class,
        DragonlordAtarka.class, TwinBolt.class})
class DragonHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a flying Dragon as though it had reach")
    void canBlockFlyingDragon() {
        Permanent dragonHunter = addCreatureReady(player2, new DragonHunter());
        Permanent dragon = addCreatureReady(player1, new ThunderbreakRegent());
        dragon.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragonHunter), indexOf(player1, dragon))));

        assertThat(dragonHunter.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block a flying non-Dragon without reach")
    void cannotBlockFlyingNonDragon() {
        Permanent dragonHunter = addCreatureReady(player2, new DragonHunter());
        Permanent flyer = addCreatureReady(player1, new TerritorialRoc());
        flyer.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragonHunter), indexOf(player1, flyer)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Protection from Dragons prevents a Dragon from blocking it")
    void protectionFromDragonsPreventsBlocking() {
        Permanent dragonHunter = addCreatureReady(player1, new DragonHunter());
        Permanent dragon = addCreatureReady(player2, new ThunderbreakRegent());
        dragonHunter.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragon), indexOf(player1, dragonHunter)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void preventsCombatDamageFromBlockedDragon() {
        Permanent dragonHunter = addCreatureReady(player2, new DragonHunter());
        Permanent dragon = addCreatureReady(player1, new ThunderbreakRegent());
        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, dragon)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragonHunter), indexOf(player1, dragon))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragonHunter);
        assertThat(dragonHunter.getMarkedDamage()).isZero();
        assertThat(dragon.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void nonDragonCanBlockDragonHunter() {
        Permanent dragonHunter = addCreatureReady(player1, new DragonHunter());
        Permanent blocker = addCreatureReady(player2, new TerritorialRoc());
        dragonHunter.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, dragonHunter))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void dragonTriggeredAbilityCannotTargetDragonHunter() {
        Permanent dragonHunter = addCreatureReady(player2, new DragonHunter());
        harness.setHand(player1, List.of(new DragonlordAtarka()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        gd.pendingETBDamageAssignments = Map.of(dragonHunter.getId(), 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(dragonHunter.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void nonDragonSpellCanTargetAndDamageDragonHunter() {
        Permanent dragonHunter = addCreatureReady(player2, new DragonHunter());
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, Map.of(dragonHunter.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dragonHunter);
        harness.assertInGraveyard(player2, "Dragon Hunter");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
