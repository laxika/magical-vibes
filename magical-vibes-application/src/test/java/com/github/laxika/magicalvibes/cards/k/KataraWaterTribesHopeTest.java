package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({KataraWaterTribesHope.class, GrizzlyBears.class})
class KataraWaterTribesHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 white Ally token")
    void createsAllyToken() {
        harness.castFromHand(player1, new KataraWaterTribesHope(), "{2}{W}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("Waterbend X sets your creatures' base power and toughness")
    void waterbendSetsOwnCreaturesToX() {
        harness.castFromHand(player1, new KataraWaterTribesHope(), "{2}{W}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent katara = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KataraWaterTribesHope)
                .findFirst()
                .orElseThrow();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent allyToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, 0, 3, null);

        assertThat(katara.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isTrue();
        assertThat(allyToken.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("X cannot be zero")
    void waterbendRequiresPositiveX() {
        harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    @DisplayName("Waterbend can be activated only during your turn")
    void waterbendRequiresYourTurn() {
        harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }
}
