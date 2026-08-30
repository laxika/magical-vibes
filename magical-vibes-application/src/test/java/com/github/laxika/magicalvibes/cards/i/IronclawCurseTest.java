package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronclawCurse.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class,
        CrawWurm.class, FountainOfYouth.class})
class IronclawCurseTest extends BaseCardTest {

    /** Giant Spider (2/4) enchanted with Ironclaw Curse, attached and on the battlefield. */
    private Permanent cursedSpider() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IronclawCurse());
        aura.setAttachedTo(spider.getId());
        return spider;
    }

    @Test
    @DisplayName("Resolving Ironclaw Curse attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

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
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant()); // 3/3

        assertThat(bls.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can't block an attacker whose power exceeds its toughness")
    void cantBlockAttackerWithPowerGreaterThanToughness() {
        Permanent spider = cursedSpider(); // 2/3 after the curse
        Permanent crawWurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm()); // 6/4

        assertThat(bls.canBlockAttacker(gd, spider, crawWurm,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can still block an attacker with lower power")
    void canBlockAttackerWithLowerPower() {
        Permanent spider = cursedSpider(); // 2/3 after the curse
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        assertThat(bls.canBlockAttacker(gd, spider, bears,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Block restriction and debuff disappear when the aura is removed")
    void restrictionStopsWhenAuraRemoved() {
        Permanent spider = cursedSpider();
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        Permanent aura = findPermanent(player1, "Ironclaw Curse");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Back to 2/4: power 3 < toughness 4, so blocking is legal again.
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
        assertThat(bls.canBlockAttacker(gd, spider, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ironclaw Curse")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new IronclawCurse()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
