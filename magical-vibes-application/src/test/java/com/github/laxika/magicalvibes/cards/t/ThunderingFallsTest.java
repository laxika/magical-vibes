package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingFalls.class, GrizzlyBears.class})
class ThunderingFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ThunderingFalls()));

        harness.playLand(player1, 0);
        Permanent falls = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falls.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        Permanent falls = addReadyFalls();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(falls.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent falls = addReadyFalls();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(falls.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyFalls() {
        Permanent falls = new Permanent(new ThunderingFalls());
        falls.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(falls);
        return falls;
    }
}
