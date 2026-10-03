package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalSpectacle.class, AirElemental.class, GrizzlyBears.class, RagingGoblin.class, GloriousAnthem.class, Ornithopter.class})
class ElementalSpectacleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates no tokens and gains no life when you control no colored permanents")
    void createsNoTokensWithNoColors() {
        castElementalSpectacle();

        assertThat(elementalTokens()).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creates one token for each distinct permanent color")
    void createsTokensForDistinctColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new AirElemental());

        castElementalSpectacle();

        List<Permanent> tokens = elementalTokens();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(5);
            assertThat(token.getEffectiveToughness()).isEqualTo(5);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
    }

    @Test
    @DisplayName("Gains life equal to the number of creatures after creating tokens")
    void gainsLifeForCreaturesAfterTokenCreation() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());

        castElementalSpectacle();

        assertThat(elementalTokens()).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Repeated colors count once while every controlled creature contributes life")
    void repeatedColorsCountOnce() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castElementalSpectacle();

        assertThat(elementalTokens()).hasSize(1);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Opponent permanents contribute neither colors nor creatures")
    void ignoresOpponentPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new AirElemental());

        castElementalSpectacle();

        assertThat(elementalTokens()).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Previously created multicolored tokens contribute both colors to the next spell")
    void countsBothColorsOfExistingTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castElementalSpectacle();
        assertThat(elementalTokens()).hasSize(1);
        harness.assertLife(player1, 22);

        castElementalSpectacle();

        assertThat(elementalTokens()).hasSize(3);
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Colorless creatures count for life without contributing a color")
    void gainsLifeWithOnlyColorlessCreatures() {
        harness.addToBattlefield(player1, new Ornithopter());

        castElementalSpectacle();

        assertThat(elementalTokens()).isEmpty();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Colored noncreature permanents contribute colors but not creatures")
    void countsColoredNoncreaturePermanents() {
        harness.addToBattlefield(player1, new GloriousAnthem());

        castElementalSpectacle();

        assertThat(elementalTokens()).hasSize(1);
        harness.assertLife(player1, 21);
    }

    private void castElementalSpectacle() {
        harness.setHand(player1, List.of(new ElementalSpectacle()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private List<Permanent> elementalTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELEMENTAL))
                .toList();
    }
}
