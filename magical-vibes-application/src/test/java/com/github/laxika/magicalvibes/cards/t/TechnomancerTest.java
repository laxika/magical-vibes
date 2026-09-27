package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Technomancer.class, Forest.class, GrizzlyBears.class, MyrRetriever.class,
        WurmcoilEngine.class})
class TechnomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling three cards, then returns artifact creatures within total mana value six")
    void millsThenReturnsArtifactCreaturesWithinManaValueCap() {
        MyrRetriever firstMyr = new MyrRetriever();
        MyrRetriever secondMyr = new MyrRetriever();
        WurmcoilEngine wurmcoil = new WurmcoilEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstMyr, secondMyr, wurmcoil, bears));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castTechnomancer();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMyr, secondMyr, wurmcoil, bears);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(firstMyr.getId(), secondMyr.getId(), wurmcoil.getId())
                .doesNotContain(bears.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstMyr.getId(), secondMyr.getId(), wurmcoil.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(firstMyr.getId(), secondMyr.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(firstMyr, secondMyr)
                .doesNotContain(wurmcoil, bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(wurmcoil.getId(), bears.getId());
    }

    @Test
    @DisplayName("The controller may return no artifact creatures")
    void mayReturnNoArtifactCreatures() {
        MyrRetriever myr = new MyrRetriever();
        harness.setGraveyard(player1, List.of(myr));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castTechnomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(myr);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(myr.getId());
    }

    private void castTechnomancer() {
        harness.setHand(player1, List.of(new Technomancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
