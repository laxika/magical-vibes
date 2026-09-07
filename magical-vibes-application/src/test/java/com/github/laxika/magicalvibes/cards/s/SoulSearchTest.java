package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SoulSearch.class, LightningBolt.class, GrizzlyBears.class, Forest.class})
class SoulSearchTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a chosen mana value 1 nonland card and creates a flying Spirit")
    void exilesCheapCardAndCreatesSpirit() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        castSoulSearch();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bolt);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getName()).isEqualTo("Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Exiles a chosen card with mana value greater than 1 without creating a token")
    void exilesExpensiveCardWithoutCreatingSpirit() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        castSoulSearch();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot choose a land from the revealed hand")
    void cannotChooseLand() {
        Forest forest = new Forest();
        harness.setHand(player2, List.of(forest));
        harness.setHand(player1, List.of(new SoulSearch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SoulSearch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSoulSearch() {
        harness.setHand(player1, List.of(new SoulSearch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class))
                .isNotNull();
    }
}
