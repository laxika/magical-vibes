package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HorncallersChant.class, GrizzlyBears.class, CallOfTheConclave.class})
class HorncallersChantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 trample Rhino, then populate copies it when it is the only creature token")
    void populateCopiesTheNewRhino() {
        harness.setHand(player1, List.of(new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> rhinos = rhinosOf(player1);
        assertThat(rhinos).hasSize(2);
        assertThat(rhinos).allSatisfy(rhino -> {
            assertThat(rhino.getCard().isToken()).isTrue();
            assertThat(rhino.getCard().getPower()).isEqualTo(4);
            assertThat(rhino.getCard().getToughness()).isEqualTo(4);
            assertThat(rhino.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        });
    }

    @Test
    @DisplayName("A nontoken creature is not a legal populate choice")
    void nontokenCreatureIsNotPopulated() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(rhinosOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("With another creature token the controller chooses which one populate copies")
    void controllerChoosesWhichTokenToCopy() {
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Soldier Token"));

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(rhinosOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Populate ignores an opponent's creature token")
    void opposingTokenIsNotPopulated() {
        harness.addToBattlefield(player2, soldierToken());
        harness.setHand(player1, List.of(new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(rhinosOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Soldier Token")).isZero();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Populate copies a real creature token without copying counters or tapped state")
    void populateCopiesOnlyCopiableCharacteristics() {
        harness.setHand(player1, List.of(new CallOfTheConclave(), new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        Permanent original = gd.playerBattlefields.get(player1.getId()).getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.tap();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                original.getId(), rhinosOf(player1).getFirst().getId());
        harness.handlePermanentChosen(player1, original.getId());

        List<Permanent> centaurs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Centaur".equals(p.getCard().getName()))
                .toList();
        assertThat(centaurs).hasSize(2);
        Permanent copy = centaurs.stream().filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(original.isTapped()).isTrue();
        assertThat(rhinosOf(player1)).hasSize(1);
    }

    private List<Permanent> rhinosOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Rhino".equals(p.getCard().getName()))
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
