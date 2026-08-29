package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Riverchurn Monument")
class RiverchurnMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Its first ability mills two cards from each chosen player")
    void firstAbilityMillsEachChosenPlayer() {
        addMonument();
        stockLibrary(player1, 10);
        stockLibrary(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Exhaust mills each chosen player based on that player's graveyard")
    void exhaustUsesEachTargetPlayersGraveyard() {
        addMonument();
        stockLibrary(player1, 10);
        stockLibrary(player2, 10);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addExhaustMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        Permanent monument = addMonument();
        stockLibrary(player1, 10);
        addExhaustMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(player1.getId()));
        harness.passBothPriorities();
        monument.untap();
        addExhaustMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addMonument() {
        return harness.addToBattlefieldAndReturn(player1, new RiverchurnMonument());
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void stockLibrary(Player player, int count) {
        List<Card> cards = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        harness.setLibrary(player, cards);
    }
}
