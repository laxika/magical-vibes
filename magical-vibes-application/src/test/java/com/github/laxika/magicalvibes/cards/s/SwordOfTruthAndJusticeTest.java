package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTruthAndJustice.class, GrizzlyBears.class})
class SwordOfTruthAndJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from white and blue")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage puts a counter on a chosen creature and proliferates")
    void combatDamagePutsCounterAndProliferates() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when the equipped creature is blocked")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addSwordReady(Player player) {
        Permanent permanent = new Permanent(new SwordOfTruthAndJustice());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
