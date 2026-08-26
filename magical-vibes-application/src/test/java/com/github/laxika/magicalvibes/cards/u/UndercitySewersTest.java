package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercitySewers.class, GrizzlyBears.class})
class UndercitySewersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UndercitySewers()));

        harness.playLand(player1, 0);
        Permanent sewers = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sewers.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        tapFor(ManaColor.BLACK);
    }

    private void tapFor(ManaColor color) {
        Permanent sewers = addReadySewers();

        harness.activateAbility(player1, 0, color == ManaColor.BLUE ? 0 : 1, null, null);

        assertThat(sewers.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadySewers() {
        Permanent sewers = new Permanent(new UndercitySewers());
        sewers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sewers);
        return sewers;
    }
}
