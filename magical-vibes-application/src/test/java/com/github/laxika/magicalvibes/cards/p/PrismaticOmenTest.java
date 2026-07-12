package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismaticOmenTest extends BaseCardTest {

    @Test
    void landYouControlGainsEveryBasicLandType() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST);
    }

    @Test
    void landYouControlCanTapForAnyColor() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.activateAbility(player1, 1, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void opponentLandsAreNotAffected() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player2, new Forest());

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonLandPermanentsAreNotAffected() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, bears);

        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST);
    }

    @Test
    void typeChangeIsLostWhenPrismaticOmenLeaves() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).removeFirst();

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes())
                .doesNotContain(CardSubtype.ISLAND, CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.PLAINS);
    }
}
