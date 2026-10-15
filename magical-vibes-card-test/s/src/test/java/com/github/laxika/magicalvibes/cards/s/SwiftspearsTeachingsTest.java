package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftspearsTeachings.class, GrizzlyBears.class})
class SwiftspearsTeachingsTest extends BaseCardTest {

    @Test
    @DisplayName("draws a card and gives the next creature spell a chosen keyword")
    void drawsAndGrantsChosenKeyword() {
        harness.setHand(player1, List.of(new SwiftspearsTeachings()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedControllerSpellCastTrigger.class))
                .hasSize(1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "It gains prowess");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.PROWESS)).isTrue();
        assertThat(gd.getDelayedActions(DelayedControllerSpellCastTrigger.class))
                .isEmpty();
    }

    @Test
    @DisplayName("the boon can choose haste")
    void choosesHaste() {
        harness.setHand(player1, List.of(new SwiftspearsTeachings()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "It gains haste");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }
}
