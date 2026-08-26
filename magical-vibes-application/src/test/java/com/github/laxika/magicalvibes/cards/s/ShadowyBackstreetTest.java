package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({ShadowyBackstreet.class, GrizzlyBears.class})
class ShadowyBackstreetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ShadowyBackstreet()));

        harness.playLand(player1, 0);
        Permanent backstreet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(backstreet.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        tapFor(ManaColor.BLACK);
    }

    private void tapFor(ManaColor color) {
        Permanent backstreet = addReadyBackstreet();

        harness.activateAbility(player1, 0, color == ManaColor.WHITE ? 0 : 1, null, null);

        assertThat(backstreet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadyBackstreet() {
        Permanent backstreet = new Permanent(new ShadowyBackstreet());
        backstreet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(backstreet);
        return backstreet;
    }
}
