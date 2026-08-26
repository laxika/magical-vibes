package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpidersManHeroicHorde.class, GrizzlyBears.class})
class SpidersManHeroicHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Web-slinging gains 3 life and creates two 2/1 Spider tokens with reach")
    void webSlingingCreatesTokensAndGainsLife() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SpidersManHeroicHorde()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedCreature);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
        });
    }

    @Test
    @DisplayName("The normal cast does not create tokens or gain life")
    void normalCastDoesNotGetWebSlingingReward() {
        harness.setHand(player1, List.of(new SpidersManHeroicHorde()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Web-slinging requires returning a tapped creature")
    void webSlingingRejectsUntappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpidersManHeroicHorde()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }
}
