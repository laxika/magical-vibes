package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GelatinousGenesis.class})
class GelatinousGenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 creates three 3/3 green Ooze tokens")
    void createsOozeTokensUsingPaidX() {
        harness.setHand(player1, List.of(new GelatinousGenesis()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Ooze");
        assertThat(tokens).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OOZE);
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Casting with X=0 creates no tokens")
    void zeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new GelatinousGenesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The other player casting with X=1 receives one untapped green 1/1 Ooze creature token")
    void otherPlayerCreatesOneOoze() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GelatinousGenesis()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castSorcery(player2, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.OOZE);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Gelatinous Genesis");
    }
}
