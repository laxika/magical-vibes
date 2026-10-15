package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.d.DreamsGrip;
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

@CardUsed({InertiaBubble.class, AncientDen.class, LumengridWarden.class, YotianSoldier.class, DreamsGrip.class})
class InertiaBubbleTest extends BaseCardTest {

    @Test
    void canTargetAndAttachToArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        castBubbleAt(artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inertia Bubble")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    void canTargetArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());

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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());
        artifact.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InertiaBubble());
        aura.setAttachedTo(artifact.getId());

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void otherArtifactUntapsNormally() {
        Permanent enchantedArtifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());
        enchantedArtifact.tap();
        otherArtifact.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InertiaBubble());
        aura.setAttachedTo(enchantedArtifact.getId());

        advanceToUpkeep(player2);

        assertThat(enchantedArtifact.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isFalse();
    }

    @Test
    void artifactUntapsAfterAuraIsRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());
        artifact.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InertiaBubble());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void ownEnchantedArtifactDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AncientDen());
        artifact.tap();

        castBubbleAt(artifact);
        advanceToUpkeep(player1);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Inertia Bubble").getAttachedTo()).isEqualTo(artifact.getId());
    }

    @Test
    void resolvingAuraDoesNotTapArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        castBubbleAt(artifact);

        assertThat(artifact.isTapped()).isFalse();
        advanceToUpkeep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void enchantedArtifactCanBeUntappedBySpell() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AncientDen());
        artifact.tap();
        castBubbleAt(artifact);

        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Inertia Bubble").getAttachedTo()).isEqualTo(artifact.getId());
    }

    private void castBubbleAt(Permanent target) {
        harness.setHand(player1, List.of(new InertiaBubble()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
