package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Cagemail.class, FountainOfYouth.class, GrizzlyBears.class})
class CagemailTest extends BaseCardTest {

    @Test
    @DisplayName("Cagemail gives the enchanted creature +2/+2")
    void boostsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cagemail());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cagemail boosts only the enchanted creature")
    void doesNotBoostOtherCreatures() {
        Permanent enchantedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cagemail());
        aura.setAttachedTo(enchantedBears.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cagemail prevents the enchanted creature from attacking")
    void preventsEnchantedCreatureFromAttacking() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Cagemail());
        aura.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cagemail still allows the enchanted creature to block")
    void allowsEnchantedCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cagemail());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Removing Cagemail restores the enchanted creature's stats and attack ability")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Cagemail());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(0));

        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cagemail can enchant a creature an opponent controls")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cagemail cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
