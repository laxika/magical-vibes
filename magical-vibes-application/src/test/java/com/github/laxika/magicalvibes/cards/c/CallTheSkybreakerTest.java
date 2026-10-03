package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallTheSkybreaker.class, Forest.class})
class CallTheSkybreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Call the Skybreaker creates a 5/5 blue and red Elemental with flying")
    void createsElementalToken() {
        harness.setHand(player1, List.of(new CallTheSkybreaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

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
        harness.assertInGraveyard(player1, "Forest");
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

        harness.assertInGraveyard(player1, "Call the Skybreaker");
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

    @Test
    @DisplayName("Both hybrid symbols can be paid with red mana")
    void castsWithRedMana() {
        harness.setHand(player1, List.of(new CallTheSkybreaker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(elementals()).hasSize(1);
        harness.assertInGraveyard(player1, "Call the Skybreaker");
    }

    @Test
    @DisplayName("Retrace can be used repeatedly, paying mana and discarding a land each time")
    void retracesRepeatedlyWithMixedHybridMana() {
        CallTheSkybreaker spell = new CallTheSkybreaker();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            int spellIndex = gd.playerGraveyards.get(player1.getId()).indexOf(spell);
            harness.castRetrace(player1, spellIndex, 0);

            harness.assertNotInGraveyard(player1, "Call the Skybreaker");
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .filteredOn(c -> c.getName().equals("Forest")).hasSize(i + 1);
            harness.passBothPriorities();

            assertThat(elementals()).hasSize(i + 1);
            harness.assertInGraveyard(player1, "Call the Skybreaker");
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Retrace still requires the full mana cost and does not discard on a rejected cast")
    void retraceRequiresMana() {
        harness.setGraveyard(player1, List.of(new CallTheSkybreaker()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Call the Skybreaker");
        assertThat(gd.stack).isEmpty();
        assertThat(elementals()).isEmpty();
    }

    @Test
    @DisplayName("Retrace cannot cast this sorcery during combat")
    void retraceRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new CallTheSkybreaker()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Call the Skybreaker");
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> elementals() {
        return findPermanents(player1, "Elemental");
    }
}
