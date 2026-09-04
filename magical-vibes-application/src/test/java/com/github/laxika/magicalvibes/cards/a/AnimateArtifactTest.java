package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Conservator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WardenOfTheWall;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimateArtifact.class, Conservator.class, Ornithopter.class, GrizzlyBears.class,
        WardenOfTheWall.class})
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
        // Conservator costs {4}, so mana value = 4.
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new Conservator());
        enchant(conservator);

        assertThat(gqs.isArtifact(gd, conservator)).isTrue();
        assertThat(gqs.isCreature(gd, conservator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, conservator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, conservator)).isEqualTo(4);

        var bonus = gqs.computeStaticBonus(gd, conservator);
        assertThat(bonus.animatedCreature()).isTrue();
        assertThat(bonus.grantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    void canCastOnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Conservator());
        harness.setHand(player1, List.of(new AnimateArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    void animatesOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Conservator());
        enchant(artifact);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    void doesNotOverrideAnotherCreatureAnimation() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        enchant(warden);
        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, warden)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
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
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new Conservator());
        Permanent aura = enchant(conservator);

        assertThat(gqs.isCreature(gd, conservator)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, conservator)).isFalse();
        assertThat(gqs.getEffectivePower(gd, conservator)).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, conservator).animatedCreature()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Animate Artifact targeting a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new Conservator()); // valid target so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new AnimateArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
