package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFinalDays.class, GiantGrowth.class, GrizzlyBears.class})
class TheFinalDaysTest extends BaseCardTest {

    @Test
    void normalCastCreatesTwoTappedHorrors() {
        harness.setHand(player1, List.of(new TheFinalDays()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        List<Permanent> horrors = horrors();
        assertThat(horrors).hasSize(2);
        assertThat(horrors).allSatisfy(horror -> assertThat(horror.isTapped()).isTrue());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("The Final Days"));
    }

    private List<Permanent> horrors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Horror"))
                .toList();
    }
}
