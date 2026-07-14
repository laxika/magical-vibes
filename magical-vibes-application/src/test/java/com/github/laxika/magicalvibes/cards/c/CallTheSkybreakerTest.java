package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallTheSkybreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Call the Skybreaker creates a 5/5 blue and red Elemental with flying")
    void createsElementalToken() {
        harness.setHand(player1, List.of(new CallTheSkybreaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> elementals = elementals();
        assertThat(elementals).hasSize(1);
        Permanent elemental = elementals.getFirst();
        assertThat(elemental.getCard().getPower()).isEqualTo(5);
        assertThat(elemental.getCard().getToughness()).isEqualTo(5);
        assertThat(elemental.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(elemental.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(elemental.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(elemental.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Retrace creates an Elemental token and discards a land")
    void retraceCreatesTokenAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new CallTheSkybreaker()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(elementals()).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Retrace returns Call the Skybreaker to the graveyard, not exile, so it can be recast")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new CallTheSkybreaker()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Call the Skybreaker"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Call the Skybreaker"));
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new CallTheSkybreaker()));
        harness.setHand(player1, List.of(new CallTheSkybreaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> elementals() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental"))
                .toList();
    }
}
