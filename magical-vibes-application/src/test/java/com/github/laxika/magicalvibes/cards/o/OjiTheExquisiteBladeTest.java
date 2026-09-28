package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
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

@CardUsed({OjiTheExquisiteBlade.class, DarkRitual.class, GrizzlyBears.class})
class OjiTheExquisiteBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with 2 life and scry 2")
    void entersWithLifeGainAndScry() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new OjiTheExquisiteBlade()));
        addOjiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
    }

    @Test
    @DisplayName("The second spell flickers up to one creature you control")
    void secondSpellFlickersTargetCreature() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The second-spell trigger may resolve without a target")
    void secondSpellMayChooseNoTarget() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }

    private void addOjiMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
