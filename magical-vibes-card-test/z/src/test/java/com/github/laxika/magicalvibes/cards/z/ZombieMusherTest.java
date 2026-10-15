package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.p.PhyrexianSoulgorger;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieMusher.class, BorealCentaur.class, PhyrexianSoulgorger.class, SnowCoveredSwamp.class, Swamp.class})
class ZombieMusherTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked while the defending player controls a snow land")
    void cantBeBlockedWithSnowLand() {
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        Permanent musher = readyAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, musher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when the defending player controls only a nonsnow land")
    void canBeBlockedWithNonsnowLand() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        Permanent musher = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, musher);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A snow creature without a snow land does not enable snow landwalk")
    void snowNonlandDoesNotEnableLandwalk() {
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        Permanent musher = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, musher);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Snow mana activates regeneration")
    void snowManaActivatesRegeneration() {
        Permanent musher = addCreatureReady(player1, new ZombieMusher());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(musher.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new ZombieMusher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The attacking player's snow land does not enable snow landwalk")
    void attackersSnowLandDoesNotEnableLandwalk() {
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        Permanent musher = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, musher);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Mana produced by a snow land pays for regeneration without tapping Zombie Musher")
    void snowLandManaPaysForRegeneration() {
        Permanent musher = addCreatureReady(player1, new ZombieMusher());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());

        harness.tapPermanent(player1, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(musher.getRegenerationShield()).isEqualTo(1);
        assertThat(musher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Zombie Musher can activate regeneration repeatedly")
    void tappedSummoningSickMusherCanRegenerateRepeatedly() {
        Permanent musher = harness.addToBattlefieldAndReturn(player1, new ZombieMusher());
        musher.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(musher.getRegenerationShield()).isEqualTo(2);
        assertThat(musher.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("The regeneration shield saves Zombie Musher from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent musher = addCreatureReady(player1, new ZombieMusher());
        addCreatureReady(player2, new PhyrexianSoulgorger());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Zombie Musher");
        assertThat(musher.isTapped()).isTrue();
        assertThat(musher.getRegenerationShield()).isZero();
        assertThat(musher.getMarkedDamage()).isZero();
        assertThat(musher.isBlocking()).isFalse();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent readyAttacker(Player player) {
        Permanent permanent = addCreatureReady(player, new ZombieMusher());
        permanent.setAttacking(true);
        return permanent;
    }
}
