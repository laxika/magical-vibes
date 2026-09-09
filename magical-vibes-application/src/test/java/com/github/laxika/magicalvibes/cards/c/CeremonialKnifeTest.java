package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeremonialKnife.class, GrizzlyBears.class, SerraAngel.class})
class CeremonialKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Ceremonial Knife gives the creature +1/+0")
    void equippingGivesPowerBoost() {
        Permanent knife = addKnifeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(knife.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature creates a Blood token when it deals combat damage to a player")
    void createsBloodTokenOnCombatDamageToPlayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addKnifeReady(player1);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Equipped creature creates a Blood token when it deals combat damage to a creature")
    void createsBloodTokenOnCombatDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addKnifeReady(player1);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    private Permanent addKnifeReady(Player player) {
        return addReadyPermanent(player, new CeremonialKnife());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
