package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GalvanicJuggernaut;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompellingDeterrence.class, Gravecrawler.class, GalvanicJuggernaut.class, Island.class, ObstinateBaloth.class})
class CompellingDeterrenceTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Zombie, bounces target and does not discard")
    void bounceWithoutZombieNoDiscard() {
        harness.addToBattlefield(player2, new GalvanicJuggernaut());
        UUID targetId = harness.getPermanentId(player2, "Galvanic Juggernaut");
        harness.setHand(player2, new ArrayList<>(List.of(new CompellingDeterrence())));

        castAt(targetId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Galvanic Juggernaut");
        harness.assertInHand(player2, "Galvanic Juggernaut");
        harness.assertInHand(player2, "Compelling Deterrence");
    }

    @Test
    @DisplayName("With a Zombie, owner discards after bounce (may discard the returned card)")
    void bounceWithZombieOwnerDiscards() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new GalvanicJuggernaut());
        UUID targetId = harness.getPermanentId(player2, "Galvanic Juggernaut");
        harness.setHand(player2, new ArrayList<>(List.of(new CompellingDeterrence())));

        castAt(targetId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        // Hand is Compelling Deterrence + bounced Galvanic Juggernaut; discard the returned creature.
        int bearsIndex = indexOf(gd.playerHands.get(player2.getId()), "Galvanic Juggernaut");
        harness.handleCardChosen(player2, bearsIndex);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Compelling Deterrence"));
        harness.assertInGraveyard(player2, "Galvanic Juggernaut");
    }

    @Test
    @DisplayName("Targeting your only Zombie does not cause a discard after it leaves")
    void targetingOnlyZombieSkipsDiscard() {
        harness.addToBattlefield(player1, new Gravecrawler());
        UUID targetId = harness.getPermanentId(player1, "Gravecrawler");
        harness.setHand(player1, new ArrayList<>(List.of(new CompellingDeterrence(), new CompellingDeterrence())));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Gravecrawler");
        harness.assertInHand(player1, "Gravecrawler");
        harness.assertInHand(player1, "Compelling Deterrence");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new CompellingDeterrence()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void returnedCardMustBeDiscardedWhenHandWasEmpty() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new GalvanicJuggernaut());
        harness.setHand(player2, List.of());

        castAt(harness.getPermanentId(player2, "Galvanic Juggernaut"));
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Galvanic Juggernaut");
        harness.assertNotInHand(player2, "Galvanic Juggernaut");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void illegalTargetDoesNotCauseDiscard() {
        harness.addToBattlefield(player1, new Gravecrawler());
        var target = harness.addToBattlefieldAndReturn(player2, new GalvanicJuggernaut());
        harness.setHand(player2, List.of(new CompellingDeterrence()));
        harness.setHand(player1, List.of(new CompellingDeterrence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Compelling Deterrence");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ownerDiscardsRatherThanCurrentController() {
        harness.addToBattlefield(player1, new Gravecrawler());
        var target = harness.addToBattlefieldAndReturn(player1, new GalvanicJuggernaut());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player2, List.of(new CompellingDeterrence()));

        castAt(target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.assertInHand(player2, "Galvanic Juggernaut");
        harness.handleCardChosen(player2, indexOf(gd.playerHands.get(player2.getId()), "Galvanic Juggernaut"));
        harness.assertInGraveyard(player2, "Galvanic Juggernaut");
    }

    @Test
    void opponentCausedDiscardAppliesBalothReplacement() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new GalvanicJuggernaut());
        harness.setHand(player2, List.of(new ObstinateBaloth()));

        castAt(harness.getPermanentId(player2, "Galvanic Juggernaut"));
        harness.handleCardChosen(player2, indexOf(gd.playerHands.get(player2.getId()), "Obstinate Baloth"));

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new CompellingDeterrence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
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
