package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfJustice.class, GrizzlyBears.class})
class DecreeOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 creates two 4/4 flying Angels")
    void hardCastCreatesAngels() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        List<Permanent> angels = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Angel".equals(permanent.getCard().getName()))
                .toList();
        assertThat(angels).hasSize(2);
        assertThat(angels).allSatisfy(angel -> {
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Cycling can pay X to create Soldiers and still draws a card")
    void cyclingPaysXCreatesSoldiersAndDraws() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Decree of Justice");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling with X=0 creates no Soldiers and still draws a card")
    void cyclingDeclinesSoldiersAndDraws() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 0);

        assertThat(countPermanents(player1, "Soldier")).isZero();
        harness.assertInGraveyard(player1, "Decree of Justice");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
