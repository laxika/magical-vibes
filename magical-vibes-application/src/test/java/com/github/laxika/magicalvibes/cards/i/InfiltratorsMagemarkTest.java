package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({InfiltratorsMagemark.class, GrizzlyBears.class, WallOfFire.class})
class InfiltratorsMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Infiltrator's Magemark boosts each enchanted creature you control")
    void boostsEnchantedCreaturesYouControl() {
        Permanent firstBears = addCreature(player1);
        Permanent secondBears = addCreature(player1);
        Permanent unenchantedBears = addCreature(player1);
        Permanent opponentBears = addCreature(player2);
        addAura(firstBears);
        addAura(secondBears);

        assertThat(gqs.getEffectivePower(gd, firstBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchanted creature cannot be blocked by a creature without defender")
    void enchantedCreatureCannotBeBlockedByNormalCreature() {
        Permanent attacker = addCreature(player1);
        addAura(attacker);
        attacker.setAttacking(true);
        addCreature(player2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("An enchanted creature can be blocked by a creature with defender")
    void enchantedCreatureCanBeBlockedByDefender() {
        Permanent attacker = addCreature(player1);
        addAura(attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreature(player2, new WallOfFire());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unenchanted creature is unaffected by Infiltrator's Magemark")
    void unenchantedCreatureIsUnaffected() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCreature(player2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCreature(Player player) {
        return addCreature(player, new GrizzlyBears());
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAura(Permanent enchantedCreature) {
        Permanent aura = new Permanent(new InfiltratorsMagemark());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

}
