package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfTheLastBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 3 creates three 1/1 white and black Spirit tokens with flying")
    void afterlifeCreatesThreeSpiritTokens() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Knight of the Last Breath");
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .toList();

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Sacrificing another nontoken creature creates a Spirit token")
    void sacrificesAnotherNontokenCreatureToCreateSpiritToken() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Knight of the Last Breath itself")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
