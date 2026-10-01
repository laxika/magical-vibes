package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupplyDemand.class, AzoriusSignet.class, CoilingOracle.class, MistralCharger.class})
class SupplyDemandTest extends BaseCardTest {

    @Test
    @DisplayName("Supply creates X Saproling tokens")
    void supplyCreatesXTokens() {
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(permanent.getCard().getPower()).isEqualTo(1);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    @DisplayName("Supply with X zero creates no tokens")
    void supplyWithZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Demand searches for a multicolored card and shuffles")
    void demandSearchesForMulticoloredCard() {
        CoilingOracle multicolored = new CoilingOracle();
        MistralCharger monocolored = new MistralCharger();
        harness.setLibrary(player1, List.of(monocolored, multicolored));
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(multicolored);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(monocolored);
    }

    @Test
    @DisplayName("Demand does not find colorless or monocolored cards")
    void demandDoesNotFindColorlessOrMonocoloredCards() {
        AzoriusSignet colorless = new AzoriusSignet();
        MistralCharger monocolored = new MistralCharger();
        harness.setLibrary(player1, List.of(colorless, monocolored));
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(colorless, monocolored);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(colorless, monocolored);
    }
}
