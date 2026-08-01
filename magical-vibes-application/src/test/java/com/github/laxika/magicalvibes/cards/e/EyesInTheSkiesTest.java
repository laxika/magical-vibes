package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EyesInTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Bird token, then populate copies it when it is the only creature token")
    void populateCopiesTheNewBird() {
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, (UUID) null);
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
    @DisplayName("A nontoken creature is not a legal populate choice")
    void nontokenCreatureIsNotPopulated() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(birdsOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("With another creature token the controller chooses which one populate copies")
    void controllerChoosesWhichTokenToCopy() {
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, (UUID) null);
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

    private List<Permanent> birdsOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Bird".equals(p.getCard().getName()))
                .toList();
    }

    private long countOf(com.github.laxika.magicalvibes.model.Player player, String name) {
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
