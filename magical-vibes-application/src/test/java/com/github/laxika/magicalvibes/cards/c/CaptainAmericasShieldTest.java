package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericasShield.class, GrizzlyBears.class})
class CaptainAmericasShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+8 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with the equipped creature taps a creature defending player controls")
    void attackingTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger only targets creatures controlled by the defending player")
    void attackTriggerOnlyTargetsDefendingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(attacker.getId(), ownCreature.getId());
    }

    private Permanent addShieldReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent shield = new Permanent(new CaptainAmericasShield());
        shield.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shield);
        return shield;
    }
}
