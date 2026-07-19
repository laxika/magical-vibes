package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MalfegorTest extends BaseCardTest {

    private List<UUID> creatureIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
    }

    private void addMalfegorMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("ETB discards controller's hand and each opponent sacrifices one creature per card")
    void discardsHandAndOpponentSacrificesOnePerCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new Forest())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Malfegor -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // One card left in hand after casting -> discarded; opponent's only creature auto-sacrificed.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with more creatures than cards discarded chooses which to sacrifice")
    void opponentChoosesWhenMoreCreaturesThanDiscards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new Forest())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Malfegor -> ETB triggers
        harness.passBothPriorities(); // resolve ETB -> pauses for opponent's choice

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, creatureIds(player2, 1));

        assertThat(creatureCount(player2)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice count scales with the number of cards discarded")
    void sacrificeCountScalesWithCardsDiscarded() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new Forest(), new Forest())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Malfegor -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Two cards discarded, opponent has exactly two creatures -> both auto-sacrificed, no choice.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Empty hand causes no discard and no opponent sacrifice")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Malfegor -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        assertThat(creatureCount(player2)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller's own creatures are not sacrificed")
    void controllerCreaturesUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new Forest())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Malfegor -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
