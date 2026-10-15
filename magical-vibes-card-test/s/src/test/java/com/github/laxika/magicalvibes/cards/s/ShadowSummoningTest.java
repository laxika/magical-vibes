package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(ShadowSummoning.class)
class ShadowSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two tapped 1/1 white Spirit creature tokens with flying")
    void createsTappedFlyingSpiritTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShadowSummoning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();

        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.isTapped()).isTrue();
            assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Tokens belong to the caster and untap normally on their untap step")
    void tokensBelongToCasterAndUntapNormally() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShadowSummoning()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player2, 0, 0);

        List<Permanent> spirits = List.copyOf(gd.playerBattlefields.get(player2.getId()));
        assertThat(spirits).hasSize(2).allSatisfy(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.isTapped()).isTrue();
            assertThat(spirit.isAttacking()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.performUntapStep(player1);
        assertThat(spirits).allSatisfy(spirit -> assertThat(spirit.isTapped()).isTrue());

        harness.performUntapStep(player2);
        assertThat(spirits).allSatisfy(spirit -> assertThat(spirit.isTapped()).isFalse());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyElementsOf(spirits);
    }
}
