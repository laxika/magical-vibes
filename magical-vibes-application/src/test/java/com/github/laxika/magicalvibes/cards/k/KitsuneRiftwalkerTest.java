package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsuneRiftwalker.class, WanderingOnes.class, IsamaruHoundOfKonda.class,
        RendFlesh.class, YamabushisFlame.class, KamiOfTwistedReflection.class})
class KitsuneRiftwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Spirit creature cannot block Kitsune Riftwalker")
    void spiritCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new KitsuneRiftwalker());
        attacker.setAttacking(true);

        addCreatureReady(player2, new WanderingOnes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Spirit creature can block Kitsune Riftwalker")
    void nonSpiritCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new KitsuneRiftwalker());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Arcane instant cannot target Kitsune Riftwalker")
    void arcaneInstantCannotTarget() {
        Permanent riftwalker = addCreatureReady(player2, new KitsuneRiftwalker());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, riftwalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Arcane instant can target Kitsune Riftwalker")
    void nonArcaneInstantCanTarget() {
        Permanent riftwalker = addCreatureReady(player2, new KitsuneRiftwalker());

        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, riftwalker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Yamabushi's Flame");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Kitsune Riftwalker");
    }

    @Test
    @DisplayName("Spirit ability cannot target Kitsune Riftwalker")
    void spiritAbilityCannotTarget() {
        Permanent kami = addCreatureReady(player1, new KamiOfTwistedReflection());
        Permanent riftwalker = addCreatureReady(player1, new KitsuneRiftwalker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, riftwalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kami, riftwalker);
    }

    @Test
    @DisplayName("Spirit combat damage to Kitsune Riftwalker is prevented")
    void spiritCombatDamageIsPrevented() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent riftwalker = addCreatureReady(player2, new KitsuneRiftwalker());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(riftwalker.getMarkedDamage()).isZero();
    }
}
