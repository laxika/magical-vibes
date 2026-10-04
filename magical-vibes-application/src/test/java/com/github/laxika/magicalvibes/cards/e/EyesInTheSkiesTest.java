package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyesInTheSkies.class, GrizzlyBears.class, CallOfTheConclave.class, Card.class})
class EyesInTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Bird token, then populate copies it when it is the only creature token")
    void populateCopiesTheNewBird() {
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        // The Bird is the controller's only creature token, so the populate choice is forced.
        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> birds = birdsOf(player1);
        assertThat(birds).hasSize(2);
        assertThat(birds).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(bird.getCard().getPower()).isEqualTo(1);
            assertThat(bird.getCard().getToughness()).isEqualTo(1);
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("A nontoken creature is not a legal populate choice")
    void nontokenCreatureIsNotPopulated() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Soldier Token"));

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(birdsOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Populate ignores an opponent's creature token")
    void opponentsTokenIsNotPopulated() {
        harness.addToBattlefield(player2, soldierToken());
        harness.setHand(player1, List.of(new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(birdsOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Soldier Token")).isZero();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Populate copies a tapped Centaur token without copying its tapped state")
    void copiesRealCentaurTokenUntapped() {
        harness.setHand(player1, List.of(new CallOfTheConclave(), new EyesInTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        UUID centaurId = harness.getPermanentId(player1, "Centaur");
        Permanent centaur = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(centaurId)).findFirst().orElseThrow();
        centaur.tap();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                centaurId, harness.getPermanentId(player1, "Bird"));
        harness.handlePermanentChosen(player1, centaurId);

        assertThat(countOf(player1, "Centaur")).isEqualTo(2);
        assertThat(birdsOf(player1)).hasSize(1);
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Centaur".equals(p.getCard().getName()) && !p.getId().equals(centaurId))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(centaur.isTapped()).isTrue();
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
