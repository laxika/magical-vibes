package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.l.LumengridWarden;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({InertiaBubble.class, AncientDen.class, LumengridWarden.class, YotianSoldier.class})
class InertiaBubbleTest extends BaseCardTest {

    @Test
    void canTargetAndAttachToArtifact() {
        harness.addToBattlefield(player2, new AncientDen());
        Permanent artifact = findPermanent(player2, "Ancient Den");

        castBubbleAt(artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inertia Bubble")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    void canTargetArtifactCreature() {
        harness.addToBattlefield(player2, new YotianSoldier());
        Permanent artifactCreature = findPermanent(player2, "Yotian Soldier");

        castBubbleAt(artifactCreature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inertia Bubble")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifactCreature.getId()));
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new LumengridWarden());

        harness.setHand(player1, List.of(new InertiaBubble()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    void enchantedArtifactDoesNotUntap() {
        harness.addToBattlefield(player2, new AncientDen());
        Permanent artifact = findPermanent(player2, "Ancient Den");
        artifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void otherArtifactUntapsNormally() {
        harness.addToBattlefield(player2, new AncientDen());
        harness.addToBattlefield(player2, new AncientDen());
        List<Permanent> artifacts = gd.playerBattlefields.get(player2.getId());
        Permanent enchantedArtifact = artifacts.get(0);
        Permanent otherArtifact = artifacts.get(1);
        enchantedArtifact.tap();
        otherArtifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(enchantedArtifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToUpkeep(player2);

        assertThat(enchantedArtifact.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isFalse();
    }

    @Test
    void artifactUntapsAfterAuraIsRemoved() {
        harness.addToBattlefield(player2, new AncientDen());
        Permanent artifact = findPermanent(player2, "Ancient Den");
        artifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isFalse();
    }

    private void castBubbleAt(Permanent target) {
        harness.setHand(player1, List.of(new InertiaBubble()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
