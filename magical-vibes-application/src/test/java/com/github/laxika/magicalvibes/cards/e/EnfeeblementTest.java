package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enfeeblement.class, FountainOfYouth.class, GiantSpider.class, GrizzlyBears.class})
class EnfeeblementTest extends BaseCardTest {

    // ===== -2/-2 boost =====

    @Test
    @DisplayName("Enchanted creature gets -2/-2")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Enfeeblement());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving Enfeeblement attaches it and applies -2/-2")
    void resolvingAttachesAndDebuffs() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == enfeeblement
                        && p.isAttached()
                        && spider.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enfeeblement goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, spider.getId());
        gd.playerBattlefields.get(player2.getId()).remove(spider);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enfeeblement");
        harness.assertNotOnBattlefield(player1, "Enfeeblement");
    }

    @Test
    @DisplayName("Enfeeblement puts a 2/2 creature into its owner's graveyard")
    void debuffKillsTwoTwoCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Enfeeblement");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Enfeeblement")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Enfeeblement")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
