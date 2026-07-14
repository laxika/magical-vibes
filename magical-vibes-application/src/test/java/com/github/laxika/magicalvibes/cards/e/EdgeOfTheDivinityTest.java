package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DeathbringerLiege;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EdgeOfTheDivinityTest extends BaseCardTest {

    private Permanent addReady(Permanent creature) {
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        return creature;
    }

    private Permanent attach(Permanent creature) {
        Permanent edge = new Permanent(new EdgeOfTheDivinity());
        edge.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(edge);
        return edge;
    }

    // ===== White enchanted creature: +1/+2 =====

    @Test
    @DisplayName("White enchanted creature gets +1/+2 only")
    void whiteCreatureGetsPlusOnePlusTwo() {
        Permanent white = addReady(new Permanent(new EliteVanguard()));
        int basePower = gqs.getEffectivePower(gd, white);
        int baseToughness = gqs.getEffectiveToughness(gd, white);

        attach(white);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(baseToughness + 2);
    }

    // ===== Black enchanted creature: +2/+1 =====

    @Test
    @DisplayName("Black enchanted creature gets +2/+1 only")
    void blackCreatureGetsPlusTwoPlusOne() {
        Permanent black = addReady(new Permanent(new ZombieGoliath()));
        int basePower = gqs.getEffectivePower(gd, black);
        int baseToughness = gqs.getEffectiveToughness(gd, black);

        attach(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(baseToughness + 1);
    }

    // ===== White-black enchanted creature: both boosts stack (+3/+3) =====

    @Test
    @DisplayName("White-black enchanted creature gets both boosts (+3/+3)")
    void whiteBlackCreatureGetsBothBoosts() {
        Permanent gold = addReady(new Permanent(new DeathbringerLiege()));
        int basePower = gqs.getEffectivePower(gd, gold);
        int baseToughness = gqs.getEffectiveToughness(gd, gold);

        attach(gold);

        assertThat(gqs.getEffectivePower(gd, gold)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, gold)).isEqualTo(baseToughness + 3);
    }

    // ===== Neither white nor black: unaffected =====

    @Test
    @DisplayName("Non-white, non-black enchanted creature gets no boost")
    void otherColorCreatureUnaffected() {
        Permanent green = addReady(new Permanent(new GrizzlyBears()));
        int basePower = gqs.getEffectivePower(gd, green);
        int baseToughness = gqs.getEffectiveToughness(gd, green);

        attach(green);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(baseToughness);
    }

    // ===== Removal restores base stats =====

    @Test
    @DisplayName("Boost wears off when Edge of the Divinity is removed")
    void boostRemovedWhenAuraLeaves() {
        Permanent white = addReady(new Permanent(new EliteVanguard()));
        int basePower = gqs.getEffectivePower(gd, white);

        Permanent edge = attach(white);
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower + 1);

        gd.playerBattlefields.get(player1.getId()).remove(edge);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower);
    }

    // ===== Casting attaches to target =====

    @Test
    @DisplayName("Resolving Edge of the Divinity attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent white = new Permanent(new EliteVanguard());
        white.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(white);

        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, white.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Edge of the Divinity")
                        && p.isAttached()
                        && p.getAttachedTo().equals(white.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Edge of the Divinity")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
