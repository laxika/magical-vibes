package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DangerousWager;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WitsEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManNewChampion.class, Censor.class, DangerousWager.class, GrizzlyBears.class,
        Peek.class, WitsEnd.class})
class SpiderManNewChampionTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToCardsDiscardedInOneEvent() {
        harness.addToBattlefield(player1, new SpiderManNewChampion());
        harness.setHand(player1, List.of(new WitsEnd(), new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void dealsDamageToAChosenCreature() {
        harness.addToBattlefield(player1, new SpiderManNewChampion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void canDealDiscardDamageToItsController() {
        harness.addToBattlefield(player1, new SpiderManNewChampion());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerWhenAnOpponentDiscards() {
        harness.addToBattlefield(player1, new SpiderManNewChampion());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerWhenDiscardingAnEmptyHand() {
        harness.addToBattlefield(player1, new SpiderManNewChampion());
        harness.setHand(player1, List.of(new DangerousWager()));
        harness.setLibrary(player1, List.of(new Peek(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 20);
    }
}
