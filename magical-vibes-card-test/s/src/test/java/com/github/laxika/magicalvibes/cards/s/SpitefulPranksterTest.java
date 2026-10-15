package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulPrankster.class, GrizzlyBears.class, Shock.class, AjaniGoldmane.class, WrathOfGod.class})
class SpitefulPranksterTest extends BaseCardTest {

    @Test
    void hasFirstStrikeDuringItsControllersTurnOnly() {
        Permanent prankster = harness.addToBattlefieldAndReturn(player1, new SpitefulPrankster());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, prankster, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, prankster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void dealsDamageToTargetPlayerWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerWhenItDiesItself() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Spiteful Prankster"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void opponentCreatureDeathCanDamageAPlaneswalkerButCannotTargetACreature() {
        Permanent prankster = harness.addToBattlefieldAndReturn(player1, new SpitefulPrankster());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        int initialLoyalty = ajani.getCounterCount(CounterType.LOYALTY);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(ajani.getId(), player1.getId(), player2.getId())
                .doesNotContain(prankster.getId(), bear.getId());
        harness.handlePermanentChosen(player1, ajani.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(initialLoyalty - 1);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void triggersForEachOtherCreatureDyingSimultaneouslyWithIt() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spiteful Prankster");
        harness.assertInGraveyard(player1, "Spiteful Prankster");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }
}
