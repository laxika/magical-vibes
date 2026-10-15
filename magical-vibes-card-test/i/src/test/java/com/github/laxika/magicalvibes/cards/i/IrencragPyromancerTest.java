package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrencragPyromancer.class, Gingerbrute.class, BakeIntoAPie.class})
class IrencragPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn deals 3 damage to a chosen player")
    void secondDrawDealsDamageToPlayerOnlyOnce() {
        harness.addToBattlefield(player1, new IrencragPyromancer());
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute(), new Gingerbrute()));
        harness.setLife(player2, 20);

        draw(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);

        draw(player1.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The second-draw trigger can deal 3 damage to a creature")
    void secondDrawDealsDamageToCreature() {
        harness.addToBattlefield(player1, new IrencragPyromancer());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Gingerbrute()).getId();
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute()));

        draw(player1.getId());
        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gingerbrute");
    }

    @Test
    void countsDrawsBeforeEnteringTheBattlefield() {
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute()));
        draw(player1.getId());
        harness.addToBattlefield(player1, new IrencragPyromancer());

        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void doesNotTriggerWhenEnteringAfterTheSecondDraw() {
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute(), new Gingerbrute()));
        draw(player1.getId());
        draw(player1.getId());
        harness.addToBattlefield(player1, new IrencragPyromancer());

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsDrawsDoNotTriggerTheAbility() {
        harness.addToBattlefield(player1, new IrencragPyromancer());
        harness.setLibrary(player2, List.of(new Gingerbrute(), new Gingerbrute()));

        draw(player2.getId());
        draw(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggersAgainOnTheOpponentsTurn() {
        harness.addToBattlefield(player1, new IrencragPyromancer());
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute(),
                new Gingerbrute(), new Gingerbrute()));
        harness.setLibrary(player2, List.of(new Gingerbrute()));
        draw(player1.getId());
        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        draw(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void triggerResolvesAfterItsSourceIsDestroyed() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new IrencragPyromancer()).getId();
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute()));
        BakeIntoAPie removal = new BakeIntoAPie();
        harness.setHand(player2, List.of(removal));
        harness.addMana(player2, ManaColor.BLACK, 4);
        draw(player1.getId());
        draw(player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Irencrag Pyromancer");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
