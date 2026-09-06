package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscerningFinancier.class})
class DiscerningFinancierTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure at upkeep when an opponent controls more lands")
    void createsTreasureWhenOpponentControlsMoreLands() {
        addCreatureReady(player1, new DiscerningFinancier());
        addLand(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(treasures(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when opponents do not control more lands")
    void doesNotCreateTreasureWhenOpponentDoesNotControlMoreLands() {
        addCreatureReady(player1, new DiscerningFinancier());
        addLand(player1);
        addLand(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(treasures(player1)).isEmpty();
    }

    @Test
    @DisplayName("Gives a Treasure to another player and draws a card")
    void givesTreasureToAnotherPlayerAndDraws() {
        Permanent financier = addCreatureReady(player1, new DiscerningFinancier());
        Permanent treasure = addTreasureToken(player1);
        Card drawnCard = new Card();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(financier), null,
                treasure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(treasure);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a non-Treasure target")
    void rejectsNonTreasureTarget() {
        Permanent financier = addCreatureReady(player1, new DiscerningFinancier());
        Permanent artifact = addPermanent(player1, artifact("Relic"));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(financier), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Treasure you control");
    }

    private Permanent addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setSubtypes(List.of(CardSubtype.TREASURE));
        treasure.setToken(true);
        return addPermanent(player, treasure);
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player) {
        Card land = new Card();
        land.setName("Land");
        land.setType(CardType.LAND);
        return addPermanent(player, land);
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Permanent> treasures(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }

    private static Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
