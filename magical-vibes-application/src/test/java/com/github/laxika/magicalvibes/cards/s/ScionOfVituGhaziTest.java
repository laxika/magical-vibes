package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScionOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand: creates a Bird token, then populate copies it")
    void castFromHandCreatesBirdAndPopulates() {
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The Bird is the controller's only creature token, so the populate choice is forced.
        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> birds = birdsOf(player1);
        assertThat(birds).hasSize(2);
        assertThat(birds).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Cast from hand: with another creature token the controller chooses what populate copies")
    void controllerChoosesWhichTokenToCopy() {
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier Token"))
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(birdsOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Entering from the graveyard rather than a hand cast makes no token")
    void enteringNotFromHandCreatesNothing() {
        harness.setGraveyard(player1, List.of(new ScionOfVituGhazi()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scion of Vitu-Ghazi");
        assertThat(birdsOf(player1)).isEmpty();
    }

    private List<Permanent> birdsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Bird".equals(p.getCard().getName()))
                .toList();
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
