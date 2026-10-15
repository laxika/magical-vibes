package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.s.ScrapworkMutt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightstonesAnimation.class, EnergyRefractor.class, Forest.class, ScrapworkMutt.class})
class MightstonesAnimationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted artifact becomes a 4/4 artifact creature and draws a card")
    void animatesArtifactAndDraws() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightstonesAnimation()));
        addManaForMightstonesAnimation();

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a nonartifact permanent")
    void cannotEnchantNonartifact() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MightstonesAnimation()));
        addManaForMightstonesAnimation();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Can animate an opposing artifact while drawing for the Aura controller")
    void animatesOpposingArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightstonesAnimation()));
        addManaForMightstonesAnimation();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Overrides an artifact creature's base power and toughness until the Aura leaves")
    void overridesExistingCreatureUntilAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScrapworkMutt());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightstonesAnimation()));
        addManaForMightstonesAnimation();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MightstonesAnimation)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.isArtifact(creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature artifact stops being a creature when the Aura leaves")
    void animationEndsWhenAuraLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightstonesAnimation()));
        addManaForMightstonesAnimation();

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MightstonesAnimation)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isArtifact(artifact)).isTrue();
        harness.assertOnBattlefield(player1, "Energy Refractor");
    }

    private void addManaForMightstonesAnimation() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
