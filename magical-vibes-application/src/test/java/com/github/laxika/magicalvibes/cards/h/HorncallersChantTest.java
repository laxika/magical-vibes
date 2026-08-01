package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HorncallersChantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 trample Rhino, then populate copies it when it is the only creature token")
    void populateCopiesTheNewRhino() {
        harness.setHand(player1, List.of(new HorncallersChant()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier Token"))
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
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
