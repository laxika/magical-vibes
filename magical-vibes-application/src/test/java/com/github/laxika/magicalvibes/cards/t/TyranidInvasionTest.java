package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TyranidInvasion.class)
class TyranidInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 3/3 green Tyranid Warrior token with trample per opponent")
    void createsTyranidWarriorsPerOpponent() {
        harness.setHand(player1, List.of(new TyranidInvasion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        List<Permanent> tokens = findPermanents(player1, "Tyranid Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(findPermanents(player2, "Tyranid Warrior")).isEmpty();
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.WARRIOR);
            assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Creates tokens for the spell controller when player two casts it")
    void createsTokensForPlayerTwo() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TyranidInvasion()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, List.of());

        assertThat(findPermanents(player2, "Tyranid Warrior")).hasSize(1);
        assertThat(findPermanents(player1, "Tyranid Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Creates two tokens when there are two opponents")
    void createsOneTokenForEachOfTwoOpponents() {
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
        harness.setHand(player1, List.of(new TyranidInvasion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());
        if (!gd.stack.isEmpty()) {
            harness.passPriority(thirdPlayer);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Tyranid Warrior")).hasSize(2);
        assertThat(findPermanents(player2, "Tyranid Warrior")).isEmpty();
        assertThat(findPermanents(thirdPlayer, "Tyranid Warrior")).isEmpty();
    }
}
