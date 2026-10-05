package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MassProduction.class})
class MassProductionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mass Production creates four 1/1 colorless Soldier artifact creature tokens")
    void resolvingCreatesSoldierTokens() {
        harness.setHand(player1, List.of(new MassProduction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(4);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Soldier");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        }
    }

    @Test
    @DisplayName("Tokens enter untapped under the caster's control only when the spell resolves")
    void tokensEnterUnderCastersControlOnResolution() {
        harness.setHand(player1, List.of(new MassProduction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mass Production");
    }
}
