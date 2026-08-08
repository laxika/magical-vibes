package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

class WakeTheReflectionsTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the controller's only creature token")
    void copiesTheOnlyCreatureToken() {
        harness.addToBattlefield(player1, token("Soldier Token"));
        harness.setHand(player1, List.of(new WakeTheReflections()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing when the controller controls no creature token")
    void doesNothingWithoutACreatureToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WakeTheReflections()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With multiple creature tokens the controller chooses which one is copied")
    void controllerChoosesWhichTokenToCopy() {
        harness.addToBattlefield(player1, token("Soldier Token"));
        harness.addToBattlefield(player1, token("Elephant Token"));
        harness.setHand(player1, List.of(new WakeTheReflections()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent elephant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elephant Token"))
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, elephant.getId());

        assertThat(countOf(player1, "Elephant Token")).isEqualTo(2);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature token is not a legal populate choice")
    void opponentTokenIsNotCopied() {
        harness.addToBattlefield(player2, token("Soldier Token"));
        harness.setHand(player1, List.of(new WakeTheReflections()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card token(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
