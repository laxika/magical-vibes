package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({PestSummoning.class, Shock.class})
class PestSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Pest tokens")
    void createsTwoPestTokens() {
        castPestSummoning();

        assertThat(findPermanents(player1, "Pest").stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Pest token death gains 1 life")
    void pestDeathGainsLife() {
        castPestSummoning();

        Permanent pest = findPermanents(player1, "Pest").stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest"))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Created Pests have the specified characteristics and enter untapped")
    void createdPestsHaveSpecifiedCharacteristics() {
        castPestSummoning();

        assertThat(findPermanents(player1, "Pest")).hasSize(2).allSatisfy(pest -> {
            assertThat(pest.getCard().isToken()).isTrue();
            assertThat(pest.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(pest.getCard().getSubtypes()).containsExactly(CardSubtype.PEST);
            assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(pest.getCard().getPower()).isEqualTo(1);
            assertThat(pest.getCard().getToughness()).isEqualTo(1);
            assertThat(pest.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can pay both hybrid symbols with green mana")
    void canPayWithGreenMana() {
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Pest")).hasSize(2);
    }

    @Test
    @DisplayName("Both Pests independently gain life when an opponent kills them")
    void bothPestsGainLifeForTheirController() {
        castPestSummoning();
        List<Permanent> pests = findPermanents(player1, "Pest");
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        for (Permanent pest : pests) {
            harness.castInstant(player2, 0, pest.getId());
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Pest")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
    }

    private void castPestSummoning() {
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
