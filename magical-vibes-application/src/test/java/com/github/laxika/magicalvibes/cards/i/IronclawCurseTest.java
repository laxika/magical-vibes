package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IronclawCurseTest extends BaseCardTest {

    /** Grizzly Bears (2/2) enchanted with Ironclaw Curse, attached and on the battlefield. */
    private Permanent cursedSpider() {
        Permanent spider = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player1.getId()).add(spider);

        Permanent aura = new Permanent(new IronclawCurse());
        aura.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return spider;
    }

    @Test
    @DisplayName("Resolving Ironclaw Curse attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent spider = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player1.getId()).add(spider);

        harness.setHand(player1, List.of(new IronclawCurse()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ironclaw Curse")
                        && spider.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -0/-1")
    void enchantedCreatureGetsDebuff() {
        Permanent spider = cursedSpider();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature can't block an attacker whose power equals its toughness")
    void cantBlockAttackerWithPowerEqualToToughness() {
        Permanent spider = cursedSpider(); // 2/3 after the curse
        Permanent hillGiant = new Permanent(new HillGiant()); // 3/3
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        assertThat(gqs.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can still block an attacker with lower power")
    void canBlockAttackerWithLowerPower() {
        Permanent spider = cursedSpider(); // 2/3 after the curse
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.canBlockAttacker(gd, spider, bears,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Block restriction and debuff disappear when the aura is removed")
    void restrictionStopsWhenAuraRemoved() {
        Permanent spider = cursedSpider();
        Permanent hillGiant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        assertThat(gqs.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ironclaw Curse"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Back to 2/4: power 3 < toughness 4, so blocking is legal again.
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
        assertThat(gqs.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ironclaw Curse")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new IronclawCurse()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
