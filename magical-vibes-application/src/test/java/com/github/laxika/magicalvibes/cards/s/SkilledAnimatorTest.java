package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkilledAnimatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB animates the target artifact into a 5/5 artifact creature")
    void etbAnimatesArtifact() {
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player1, "Darksteel Ingot");
        harness.castCreature(player1, 0, 0, ingotId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        Permanent ingot = gqs.findPermanentById(gd, ingotId);

        assertThat(gqs.isCreature(gd, ingot)).isTrue();
        assertThat(ingot.getEffectivePower()).isEqualTo(5);
        assertThat(ingot.getEffectiveToughness()).isEqualTo(5);
        assertThat(ingot.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(gd.sourceLinkedAnimations).containsKey(ingotId);
    }

    @Test
    @DisplayName("The artifact reverts when Skilled Animator leaves the battlefield")
    void artifactRevertsWhenAnimatorLeaves() {
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player1, "Darksteel Ingot");
        harness.castCreature(player1, 0, 0, ingotId);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ingotId = harness.getPermanentId(player2, "Darksteel Ingot");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ingotId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
