package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmancipationAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts for any permanent you control, including itself")
    void promptIncludesAllOwnPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EmancipationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof EmancipationAngel)
                .findFirst()
                .orElseThrow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(angel.getId(), artifact.getId(), creature.getId())
                .doesNotContain(opponentPermanent.getId());
    }

    @Test
    @DisplayName("Chosen permanent is returned to its owner's hand")
    void chosenPermanentReturnedToHand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FireDiamond());

        harness.setHand(player1, List.of(new EmancipationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof FireDiamond);
    }

    @Test
    @DisplayName("It can return itself when it is the only permanent you control")
    void canReturnItself() {
        harness.setHand(player1, List.of(new EmancipationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof EmancipationAngel)
                .findFirst()
                .orElseThrow();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(angel.getId());

        harness.handlePermanentChosen(player1, angel.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof EmancipationAngel);
    }
}
