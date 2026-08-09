package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

class InertiaBubbleTest extends BaseCardTest {

    @Test
    void canTargetAndAttachToArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new InertiaBubble()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inertia Bubble")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new InertiaBubble()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    void enchantedArtifactDoesNotUntap() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");
        artifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void otherArtifactUntapsNormally() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        List<Permanent> artifacts = gd.playerBattlefields.get(player2.getId());
        Permanent enchantedArtifact = artifacts.get(0);
        Permanent otherArtifact = artifacts.get(1);
        enchantedArtifact.tap();
        otherArtifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(enchantedArtifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(enchantedArtifact.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isFalse();
    }

    @Test
    void artifactUntapsAfterAuraIsRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");
        artifact.tap();

        Permanent aura = new Permanent(new InertiaBubble());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
