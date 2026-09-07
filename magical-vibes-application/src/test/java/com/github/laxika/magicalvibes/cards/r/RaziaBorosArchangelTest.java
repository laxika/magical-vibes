package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaziaBorosArchangel.class, GrizzlyBears.class, ProdigalPyromancer.class, LightningBolt.class})
class RaziaBorosArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next three damage from a controlled creature to another creature")
    void redirectsNextThreeDamage() {
        Permanent razia = addReady(player1, new RaziaBorosArchangel());
        Permanent protectedCreature = addReadyStats(player1, 4, 4);
        Permanent destination = addReadyStats(player2, 5, 5);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, razia), 0,
                List.of(protectedCreature.getId(), destination.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(3);

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Requires the protected and destination creatures to be different")
    void requiresDifferentTargets() {
        Permanent razia = addReady(player1, new RaziaBorosArchangel());
        Permanent creature = addReadyStats(player1, 4, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, indexOf(player1, razia), 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReady(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
