package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkilledAnimator.class, Manalith.class, Forest.class, Skyscanner.class})
class SkilledAnimatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB animates the target artifact into a 5/5 artifact creature")
    void etbAnimatesArtifact() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player1, "Manalith");
        harness.castCreature(player1, 0, 0, ingotId);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        Permanent ingot = gqs.findPermanentById(gd, ingotId);

        assertThat(gqs.isCreature(gd, ingot)).isTrue();
        assertThat(ingot.getEffectivePower()).isEqualTo(5);
        assertThat(ingot.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.isArtifact(gd, ingot)).isTrue();
        assertThat(gd.sourceLinkedAnimations).containsKey(ingotId);
    }

    @Test
    @DisplayName("The artifact reverts when Skilled Animator leaves the battlefield")
    void artifactRevertsWhenAnimatorLeaves() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player1, "Manalith");
        harness.castCreature(player1, 0, 0, ingotId);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        Permanent animator = findPermanent(player1, "Skilled Animator");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, animator));

        Permanent ingot = gqs.findPermanentById(gd, ingotId);
        assertThat(gqs.isCreature(gd, ingot)).isFalse();
        assertThat(ingot.isPermanentlyAnimated()).isFalse();
        assertThat(gd.sourceLinkedAnimations).isEmpty();
    }

    @Test
    @DisplayName("A land is not a legal target")
    void landIsNotLegalTarget() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact is not a legal target")
    void opponentArtifactIsNotLegalTarget() {
        harness.addToBattlefield(player2, new Manalith());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player2, "Manalith");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ingotId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The artifact is not animated if the source leaves before the trigger resolves")
    void sourceLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        UUID artifactId = harness.getPermanentId(player1, "Manalith");

        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();
        Permanent animator = findPermanent(player1, "Skilled Animator");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, animator));
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, gqs.findPermanentById(gd, artifactId))).isFalse();
    }

    @Test
    @DisplayName("An artifact creature retains flying and returns to its original power and toughness")
    void artifactCreatureRetainsAbilitiesAndReverts() {
        harness.addToBattlefield(player1, new Skyscanner());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        UUID artifactId = harness.getPermanentId(player1, "Skyscanner");

        harness.castCreature(player1, 0, 0, artifactId);
        resolveAllTriggers();
        Permanent artifact = gqs.findPermanentById(gd, artifactId);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isTrue();

        Permanent animator = findPermanent(player1, "Skilled Animator");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, animator));

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An artifact stays animated while either of two Animators remains")
    void overlappingAnimationsEndIndependently() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new SkilledAnimator(), new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID artifactId = harness.getPermanentId(player1, "Manalith");

        harness.castCreature(player1, 0, 0, artifactId);
        resolveAllTriggers();
        Permanent firstAnimator = findPermanent(player1, "Skilled Animator");
        harness.castCreature(player1, 0, 0, artifactId);
        resolveAllTriggers();
        Permanent secondAnimator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkilledAnimator)
                .filter(permanent -> !permanent.getId().equals(firstAnimator.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, secondAnimator));
        Permanent artifact = gqs.findPermanentById(gd, artifactId);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, firstAnimator));
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }
}
