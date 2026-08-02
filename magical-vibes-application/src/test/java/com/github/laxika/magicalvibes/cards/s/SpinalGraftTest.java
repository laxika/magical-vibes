package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpinalGraftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spinal Graft attaches it and gives the creature +3/+3")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpinalGraft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent graft = findPermanent(player1, "Spinal Graft");
        assertThat(graft.isAttached()).isTrue();
        assertThat(graft.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off when Spinal Graft leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent graft = new Permanent(new SpinalGraft());
        graft.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(graft);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(graft);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature is destroyed when it becomes the target of a spell")
    void destroyedWhenEnchantedCreatureTargetedBySpell() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent graft = new Permanent(new SpinalGraft());
        graft.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(graft);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature destroyed this way can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);
        Permanent graft = new Permanent(new SpinalGraft());
        graft.setAttachedTo(skeletons.getId());
        gd.playerBattlefields.get(player1.getId()).add(graft);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Targeting Spinal Graft itself does not trigger the destruction")
    void doesNotTriggerWhenAuraItselfIsTargeted() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent graft = new Permanent(new SpinalGraft());
        graft.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(graft);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, graft.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Spinal Graft")
    void cannotTargetNonCreature() {
        // A legal creature target must exist somewhere, or the aura is unplayable before targeting
        // is ever validated (CR 601.2c) and the cast fails with the wrong message.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SpinalGraft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
