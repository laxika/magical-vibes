package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianBoonTest extends BaseCardTest {

    private Permanent addReady(Permanent creature) {
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        return creature;
    }

    private Permanent attach(Permanent creature) {
        Permanent boon = new Permanent(new PhyrexianBoon());
        boon.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(boon);
        return boon;
    }

    @Test
    @DisplayName("Black enchanted creature gets +2/+1")
    void blackCreatureGetsBoost() {
        Permanent black = addReady(new Permanent(new ZombieGoliath()));
        int basePower = gqs.getEffectivePower(gd, black);
        int baseToughness = gqs.getEffectiveToughness(gd, black);

        attach(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Nonblack enchanted creature gets -1/-2 instead")
    void nonBlackCreatureGetsPenalty() {
        Permanent red = addReady(new Permanent(new HillGiant()));
        int basePower = gqs.getEffectivePower(gd, red);
        int baseToughness = gqs.getEffectiveToughness(gd, red);

        attach(red);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(basePower - 1);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(baseToughness - 2);
    }

    @Test
    @DisplayName("Modification wears off when Phyrexian Boon leaves the battlefield")
    void boostRemovedWhenAuraLeaves() {
        Permanent black = addReady(new Permanent(new ZombieGoliath()));
        int basePower = gqs.getEffectivePower(gd, black);

        Permanent boon = attach(black);
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);

        gd.playerBattlefields.get(player1.getId()).remove(boon);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Resolving Phyrexian Boon attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new PhyrexianBoon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Boon")
                        && p.isAttached()
                        && p.getAttachedTo().equals(giant.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new PhyrexianBoon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
