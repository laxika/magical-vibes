package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TemptWithVengeance.class)
class TemptWithVengeanceTest extends BaseCardTest {

    @Test
    void createsBaseTokensAndBonusTokensWhenOpponentAccepts() {
        castWithX(2);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(4);
        assertThat(countPermanents(player2, "Elemental")).isEqualTo(2);
        assertThat(elementalTokens(player1)).allSatisfy(this::assertHastyOneOne);
        assertThat(elementalTokens(player2)).allSatisfy(this::assertHastyOneOne);
    }

    @Test
    void createsOnlyBaseTokensWhenOpponentDeclines() {
        castWithX(2);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    void zeroXCreatesNoTokensEvenWhenOpponentAccepts() {
        castWithX(0);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(elementalTokens(player1)).isEmpty();
        assertThat(elementalTokens(player2)).isEmpty();
    }

    @Test
    void waitsForEveryOpponentDecisionBeforeCreatingOfferedTokens() {
        Player thirdPlayer = new Player(UUID.randomUUID(), "Charlie");
        UUID id = thirdPlayer.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);

        castWithX(2);
        if (!gd.interaction.isAwaitingInput()) {
            gs.passPriority(gd, thirdPlayer);
        }

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(elementalTokens(player2)).isEmpty();
        assertThat(elementalTokens(thirdPlayer)).isEmpty();

        harness.handleMayAbilityChosen(thirdPlayer, true);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(6);
        assertThat(countPermanents(player2, "Elemental")).isEqualTo(2);
        assertThat(countPermanents(thirdPlayer, "Elemental")).isEqualTo(2);
    }

    private void castWithX(int xValue) {
        harness.setHand(player1, List.of(new TemptWithVengeance()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private List<Permanent> elementalTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .toList();
    }

    private void assertHastyOneOne(Permanent token) {
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }
}
