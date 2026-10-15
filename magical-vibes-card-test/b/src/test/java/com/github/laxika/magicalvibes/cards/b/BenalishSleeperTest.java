package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishSleeper.class, GrizzlyBears.class, GiantSpider.class})
class BenalishSleeperTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotMakePlayersSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new BenalishSleeper(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Sleeper");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedMakesEachPlayerSacrificeACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BenalishSleeper()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Benalish Sleeper");
        harness.assertInGraveyard(player1, "Benalish Sleeper");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void eachPlayerChoosesWhichCreatureToSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BenalishSleeper()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void sacrificesWaitUntilBothPlayersHaveChosen() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BenalishSleeper()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Sleeper");
        harness.assertNotInGraveyard(player1, "Benalish Sleeper");

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertInGraveyard(player1, "Benalish Sleeper");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void bothPlayersChooseInTurnOrderBeforeEitherCreatureIsSacrificed() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BenalishSleeper()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Benalish Sleeper");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void opponentWithNoCreaturesDoesNotPreventControllerSacrificing() {
        harness.setHand(player1, List.of(new BenalishSleeper()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Benalish Sleeper");
        harness.assertInGraveyard(player1, "Benalish Sleeper");
        assertThat(gd.stack).isEmpty();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
