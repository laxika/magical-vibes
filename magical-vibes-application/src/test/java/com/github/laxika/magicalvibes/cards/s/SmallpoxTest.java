package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CardType;
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

@DisplayName("Smallpox")
class SmallpoxTest extends BaseCardTest {

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private void cast() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player loses 1 life")
    void eachPlayerLosesOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.setHand(player1, new ArrayList<>(List.of(new Smallpox())));
        harness.setHand(player2, new ArrayList<>());

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each player discards a card of their choice")
    void eachPlayerDiscardsACard() {
        harness.setHand(player1, new ArrayList<>(List.of(new Smallpox(), new Peek(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player sacrifices a creature; a player with one creature loses it without a prompt")
    void eachPlayerSacrificesACreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new Smallpox())));
        harness.setHand(player2, new ArrayList<>());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent p2Bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(p2Bears);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        cast();

        // Player1 has a single creature -> auto-sacrificed; player2 has two -> prompted.
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, p2Bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each player sacrifices a land of their choice")
    void eachPlayerSacrificesALand() {
        harness.setHand(player1, new ArrayList<>(List.of(new Smallpox())));
        harness.setHand(player2, new ArrayList<>());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Runs all four steps in order for the caster")
    void runsAllFourStepsInOrder() {
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>(List.of(new Smallpox(), new Peek(), new Forest())));
        harness.setHand(player2, new ArrayList<>());
        harness.addToBattlefield(player1, new GrizzlyBears());
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Only creature -> sacrificed without a prompt; the land choice comes next.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(0);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(landCount(player1)).isEqualTo(1);
    }
}
