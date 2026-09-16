package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Card;
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

@DisplayName("Epicenter")
@CardUsed({Epicenter.class, Forest.class, WildMongrel.class})
class EpicenterTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices one land below threshold")
    void targetPlayerSacrificesOneLandBelowThreshold() {
        List<Permanent> lands = addLands(player2, 2);
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(lands.get(0).getId()));

        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(landCount(player1)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Threshold sacrifices all lands each player controls")
    void thresholdSacrificesAllLands() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addLands(player1, 2);
        addLands(player2, 3);
        harness.addToBattlefield(player2, new WildMongrel());

        cast();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
        assertThat(countPermanents(player2, "Wild Mongrel")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Six cards in the controller's graveyard do not enable threshold")
    void sixGraveyardCardsDoNotEnableThreshold() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        List<Permanent> lands = addLands(player2, 2);

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(lands.get(0).getId()));

        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(landCount(player1)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(TajuruPreserver.class)
    @DisplayName("Threshold respects an opponent's sacrifice prevention")
    void thresholdRespectsOpponentSacrificePrevention() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addLands(player1, 2);
        addLands(player2, 2);
        harness.addToBattlefield(player2, new TajuruPreserver());

        cast();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isEqualTo(2);
        assertThat(countPermanents(player2, "Tajuru Preserver")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        List<Permanent> lands = addLands(player2, 2);
        addLands(player1, 1);

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(lands.get(1).getId()));

        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(landCount(player1)).isEqualTo(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new Epicenter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private List<Permanent> addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }

    private List<Card> graveyardWithSevenCards() {
        return graveyardWithCards(7);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new Forest())
                .toList();
    }
}
