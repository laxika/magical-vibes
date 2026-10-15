package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QutrubForayer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFinalDays.class, GiantGrowth.class, GrizzlyBears.class, QutrubForayer.class})
class TheFinalDaysTest extends BaseCardTest {

    @Test
    void normalCastCreatesTwoTappedHorrors() {
        harness.setHand(player1, List.of(new TheFinalDays()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> horrors = horrors();
        assertThat(horrors).hasSize(2);
        assertThat(horrors).allSatisfy(horror -> {
            assertThat(horror.isTapped()).isTrue();
            assertThat(horror.getCard().getPower()).isEqualTo(2);
            assertThat(horror.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    void flashbackCreatesOneHorrorPerCreatureCardInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new TheFinalDays(), new GrizzlyBears(), new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        List<Permanent> horrors = horrors();
        assertThat(horrors).hasSize(2);
        assertThat(horrors).allSatisfy(horror -> assertThat(horror.isTapped()).isTrue());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("The Final Days"));
    }

    @Test
    void flashbackWithNoCreatureCardsCreatesNoTokens() {
        harness.setGraveyard(player1, List.of(new TheFinalDays(), new TheFinalDays()));
        harness.setGraveyard(player2, List.of(new QutrubForayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(horrors()).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void flashbackCountsOnlyControllersCreatureCardsAndReplacesNormalTokens() {
        harness.setGraveyard(player1, List.of(new TheFinalDays(),
                new QutrubForayer(), new QutrubForayer(), new QutrubForayer(), new TheFinalDays()));
        harness.setGraveyard(player2, List.of(new QutrubForayer(), new QutrubForayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(horrors()).hasSize(3).allSatisfy(horror -> {
            assertThat(horror.isTapped()).isTrue();
            assertThat(horror.getCard().getPower()).isEqualTo(2);
            assertThat(horror.getCard().getToughness()).isEqualTo(2);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void flashbackCountsCreatureCardsAtResolution() {
        harness.setGraveyard(player1, List.of(new TheFinalDays(), new QutrubForayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0);
        harness.setGraveyard(player1, List.of(
                new QutrubForayer(), new QutrubForayer(), new QutrubForayer()));
        harness.passBothPriorities();

        assertThat(horrors()).hasSize(3);
    }

    @Test
    void normalCastIgnoresCreatureCountInGraveyard() {
        harness.setHand(player1, List.of(new TheFinalDays()));
        harness.setGraveyard(player1, List.of(
                new QutrubForayer(), new QutrubForayer(), new QutrubForayer()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(horrors()).hasSize(2);
        harness.assertInGraveyard(player1, "The Final Days");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private List<Permanent> horrors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Horror"))
                .toList();
    }
}
