package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.g.GixianInfiltrator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevourFlesh.class, GrizzlyBears.class, GiantSpider.class, AssaultSuit.class, GixianInfiltrator.class})
class DevourFleshTest extends BaseCardTest {

    @Test
    void targetPlayerSacrificesCreatureAndGainsLifeEqualToToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void targetPlayerChoosesCreatureWhenTheyControlMultiple() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        int lifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, spider.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(spider);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void canTargetTheCaster() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void noCreaturesMeansNoLifeGain() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Devour Flesh");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lifeGainUsesToughnessIncludingCountersBeforeSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 25);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureThatCannotBeSacrificedRemainsOnBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new AssaultSuit());
        suit.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void chosenSacrificeTriggersGixianInfiltrator() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player2, new GixianInfiltrator());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 22);
        assertThat(infiltrator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
