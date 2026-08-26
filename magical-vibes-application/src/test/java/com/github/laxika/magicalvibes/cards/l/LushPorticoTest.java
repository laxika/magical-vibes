package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LushPortico.class, GrizzlyBears.class})
class LushPorticoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LushPortico()));

        harness.playLand(player1, 0);
        Permanent portico = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(portico.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        Permanent portico = addReadyPortico();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(portico.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        Permanent portico = addReadyPortico();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(portico.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private Permanent addReadyPortico() {
        Permanent portico = new Permanent(new LushPortico());
        portico.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(portico);
        return portico;
    }
}
