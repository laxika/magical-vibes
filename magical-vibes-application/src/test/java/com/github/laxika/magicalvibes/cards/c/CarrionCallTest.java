package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionCall.class})
class CarrionCallTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and resolving Carrion Call creates two Phyrexian Insect tokens")
    void resolvingCreatesTwoTokens() {
        harness.setHand(player1, List.of(new CarrionCall()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Phyrexian Insect"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Created tokens are 1/1 with infect")
    void tokensHaveCorrectStats() {
        harness.setHand(player1, List.of(new CarrionCall()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Phyrexian Insect"))
                .toList();

        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.INSECT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.INFECT)).isTrue();
            assertThat(token.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Carrion Call goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new CarrionCall()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Carrion Call");
    }

    @Test
    @DisplayName("Tokens enter under the controller's control")
    void tokensEnterUnderControllerControl() {
        harness.setHand(player1, List.of(new CarrionCall()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Phyrexian Insect"))
                .count()).isEqualTo(2);

        // No tokens on opponent's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }

    @Test
    @DisplayName("Created Insects deal poison counters instead of reducing life")
    void tokensDealPoisonCounters() {
        harness.setHand(player1, List.of(new CarrionCall()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.setLife(player2, 20);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId());
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            token.setSummoningSick(false);
            token.setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
