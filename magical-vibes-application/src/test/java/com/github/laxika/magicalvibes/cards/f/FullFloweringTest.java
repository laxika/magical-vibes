package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FullFlowering.class, SakuraTribeElder.class})
class FullFloweringTest extends BaseCardTest {

    @Test
    @DisplayName("Populates X times, with a new choice for each repetition")
    void populatesXTimes() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        harness.setHand(player1, List.of(new FullFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent firstCopy = findToken(player1, "Soldier Token");
        harness.handlePermanentChosen(player1, firstCopy.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent secondCopy = findToken(player1, "Soldier Token");
        harness.handlePermanentChosen(player1, secondCopy.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(4);
    }

    @Test
    @DisplayName("Does nothing when X is zero")
    void doesNothingWhenXIsZero() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        harness.setHand(player1, List.of(new FullFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Can copy a token created by an earlier populate repetition")
    void canCopyNewlyCreatedToken() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        harness.setHand(player1, List.of(new FullFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 2);

        Permanent newToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, newToken.getId());

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Full Flowering");
    }

    @Test
    @DisplayName("Does nothing with no creature tokens, even if the opponent has one")
    void ignoresIneligiblePermanents() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        Card artifactToken = creatureToken("Treasure Token");
        artifactToken.setType(CardType.ARTIFACT);
        harness.addToBattlefield(player1, artifactToken);
        harness.addToBattlefield(player2, creatureToken("Soldier Token"));
        harness.setHand(player1, List.of(new FullFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Full Flowering");
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private Permanent findToken(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
