package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Smallpox")
@CardUsed({Smallpox.class, AshcoatBear.class, Forest.class})
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
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Each player loses 1 life")
    void eachPlayerLosesOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new Smallpox()));
        harness.setHand(player2, List.of());

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each player discards a card of their choice")
    void eachPlayerDiscardsACard() {
        harness.setHand(player1, List.of(new Smallpox(), new AshcoatBear(), new Forest()));
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));

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
        harness.setHand(player1, List.of(new Smallpox()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AshcoatBear());
        Permanent p2Bears = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addToBattlefield(player2, new AshcoatBear());

        cast();

        // Player1 has a single creature -> auto-sacrificed; player2 has two -> prompted.
        assertThat(countPermanents(player1, "Ashcoat Bear")).isEqualTo(0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, p2Bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player2, "Ashcoat Bear")).isEqualTo(1);
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Each player sacrifices a land of their choice")
    void eachPlayerSacrificesALand() {
        harness.setHand(player1, List.of(new Smallpox()));
        harness.setHand(player2, List.of());
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
    @DisplayName("Each player chooses a land to sacrifice")
    void eachPlayerChoosesALand() {
        harness.setHand(player1, List.of(new Smallpox()));
        harness.setHand(player2, List.of());
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player2, new Forest());
        }

        cast();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Runs all four steps in order for the caster")
    void runsAllFourStepsInOrder() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Smallpox(), new AshcoatBear(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AshcoatBear());
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Only creature -> sacrificed without a prompt; the land choice comes next.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(countPermanents(player1, "Ashcoat Bear")).isEqualTo(0);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(landCount(player1)).isEqualTo(1);
    }
}
