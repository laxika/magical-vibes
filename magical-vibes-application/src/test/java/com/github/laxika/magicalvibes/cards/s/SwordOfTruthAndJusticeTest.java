package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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
    @DisplayName("Combat damage puts a counter on a chosen creature, then proliferates")
    void combatDamagePutsCounterThenProliferates() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent proliferateTarget = addCreatureReady(player1, new GrizzlyBears());
        proliferateTarget.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(proliferateTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches the Sword to a creature you control")
    void equipsToCreatureYouControl() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addSwordReady(Player player) {
        Permanent permanent = new Permanent(new SwordOfTruthAndJustice());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
