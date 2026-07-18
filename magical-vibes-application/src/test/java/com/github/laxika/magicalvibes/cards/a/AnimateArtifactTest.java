package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimateArtifactTest extends BaseCardTest {

    private Permanent enchant(Permanent artifact) {
        Permanent aura = new Permanent(new AnimateArtifact());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted noncreature artifact becomes an artifact creature with P/T equal to its mana value")
    void animatesNoncreatureArtifact() {
        // Icy Manipulator costs {4}, so mana value = 4.
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icy = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(icy);

        assertThat(gqs.isCreature(gd, icy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, icy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, icy)).isEqualTo(4);

        var bonus = gqs.computeStaticBonus(gd, icy);
        assertThat(bonus.animatedCreature()).isTrue();
        assertThat(bonus.grantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("An artifact that is already a creature keeps its printed P/T")
    void doesNotAnimateArtifactCreature() {
        // Ornithopter is already a 0/2 artifact creature; the "isn't a creature" clause fails.
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent thopter = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(thopter);

        assertThat(gqs.isCreature(gd, thopter)).isTrue();
        // Not overridden to its mana value (0/0); it keeps its printed 0/2.
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, thopter).animatedCreature()).isFalse();
    }

    @Test
    @DisplayName("Artifact reverts to a non-creature when Animate Artifact leaves the battlefield")
    void artifactRevertsWhenAuraLeaves() {
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icy = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = enchant(icy);

        assertThat(gqs.isCreature(gd, icy)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, icy)).isFalse();
        assertThat(gqs.getEffectivePower(gd, icy)).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, icy).animatedCreature()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Animate Artifact targeting a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new IcyManipulator()); // valid target so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new AnimateArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
