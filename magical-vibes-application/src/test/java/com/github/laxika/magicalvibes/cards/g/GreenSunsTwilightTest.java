package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreenSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("With X less than 5, puts the chosen creature and land into hand")
    void putsCardsIntoHandBelowThreshold() {
        resolveSpell(4);

        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With X at least 5, the hand mode puts both chosen cards into hand")
    void highXHandMode() {
        resolveSpell(5);

        harness.handleListChoice(player1, "Put the chosen cards into your hand");
        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With X at least 5, the battlefield mode puts both chosen cards onto the battlefield")
    void highXBattlefieldMode() {
        resolveSpell(5);

        harness.handleListChoice(player1, "Put the chosen cards onto the battlefield");
        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(p -> p.getCard().getName()))
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Grizzly Bears", "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveSpell(int x) {
        harness.setHand(player1, List.of(new GreenSunsTwilight()));
        harness.addMana(player1, ManaColor.GREEN, x + 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Shock()));
        harness.castSorceryForX(player1, 0, x, Map.of());
        harness.passBothPriorities();

        if (x >= 5) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        } else {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        }
    }

    private void chooseLibraryCard(int index) {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
