package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompellingDeterrenceTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Zombie, bounces target and does not discard")
    void bounceWithoutZombieNoDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAt(targetId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("With a Zombie, owner discards after bounce (may discard the returned card)")
    void bounceWithZombieOwnerDiscards() {
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAt(targetId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        // Hand is Peek + bounced Grizzly Bears; discard the returned creature.
        int bearsIndex = indexOf(gd.playerHands.get(player2.getId()), "Grizzly Bears");
        harness.handleCardChosen(player2, bearsIndex);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Targeting your only Zombie does not cause a discard after it leaves")
    void targetingOnlyZombieSkipsDiscard() {
        harness.addToBattlefield(player1, new DiregrafGhoul());
        UUID targetId = harness.getPermanentId(player1, "Diregraf Ghoul");
        harness.setHand(player1, new ArrayList<>(List.of(new CompellingDeterrence(), new Peek())));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Diregraf Ghoul"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Diregraf Ghoul"))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new CompellingDeterrence()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new CompellingDeterrence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private static int indexOf(List<? extends com.github.laxika.magicalvibes.model.Card> hand, String name) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Card not in hand: " + name);
    }
}
