package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JadziStewardOfFateOraclesGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Jadzi draws two cards, discards two cards, and prepares it")
    void entersPreparedAndRummages() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jadzi = findPermanent(player1, "Jadzi, Steward of Fate");
        assertThat(jadzi.isPrepared()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting Oracle's Gift creates X Fractals and puts X counters on each")
    void createsFractalsWithXCounters() {
        Permanent jadzi = castJadzi();
        UUID copyId = jadzi.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCardFromExile(gd, player1, copyId, 2, null);
        harness.passBothPriorities();

        List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FRACTAL))
                .toList();
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(fractal.getEffectivePower()).isEqualTo(2);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(jadzi.isPrepared()).isFalse();
    }

    private Permanent castJadzi() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        return findPermanent(player1, "Jadzi, Steward of Fate");
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
