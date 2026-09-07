package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoEscape.class, Opt.class, ChandraNalaar.class, GrizzlyBears.class})
class NoEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell, exiles it, and starts scrying 1")
    void countersCreatureSpellAndExilesIt() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Counters a planeswalker spell and scries 1")
    void countersPlaneswalkerSpell() {
        ChandraNalaar chandra = new ChandraNalaar();
        harness.setHand(player1, List.of(chandra));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Chandra Nalaar");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetOtherSpell() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, opt.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Completing the scry finishes resolving No Escape")
    void completingScryFinishesResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        harness.assertInGraveyard(player2, "No Escape");
    }
}
