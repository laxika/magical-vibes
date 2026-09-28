package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Annihilate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TooferKeeperOfTheFullGrip.class, Annihilate.class, Divination.class,
        GrizzlyBears.class, Opt.class})
class TooferKeeperOfTheFullGripTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 2 life when a spell gives its controller card advantage")
    void triggersOnCardAdvantage() {
        addCreatureReady(player1, new TooferKeeperOfTheFullGrip());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger when a spell does not give its controller card advantage")
    void doesNotTriggerWithoutCardAdvantage() {
        addCreatureReady(player1, new TooferKeeperOfTheFullGrip());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts destroying an opponent's creature and drawing a card as card advantage")
    void triggersOnRemovalAndDraw() {
        addCreatureReady(player1, new TooferKeeperOfTheFullGrip());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
