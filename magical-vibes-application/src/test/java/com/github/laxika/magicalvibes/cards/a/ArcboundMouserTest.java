package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundMouser.class, BronzeSable.class, GrizzlyBears.class, Assassinate.class})
class ArcboundMouserTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArcboundMouser()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mouser = findPermanent(player1, "Arcbound Mouser");
        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayPutItsCounterOnTargetArtifactCreatureWhenItDies() {
        Permanent mouser = addCreatureReady(player1, new ArcboundMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        mouser.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyMouser(mouser);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularCanBeDeclinedAfterChoosingATarget() {
        Permanent mouser = addCreatureReady(player1, new ArcboundMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        mouser.tap();
        Permanent recipient = addCreatureReady(player1, new ArcboundMouser());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyMouser(mouser);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularTransfersAllCountersToAnOpponentsArtifactCreature() {
        Permanent mouser = addCreatureReady(player1, new ArcboundMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        mouser.tap();
        Permanent recipient = addCreatureReady(player2, new ArcboundMouser());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyMouser(mouser);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void lifelinkGainsLifeForCombatDamageDealt() {
        Permanent mouser = addCreatureReady(player1, new ArcboundMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void modularChoiceDescribesPlusOneCounters() {
        Permanent mouser = addCreatureReady(player1, new ArcboundMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        mouser.tap();
        Permanent recipient = addCreatureReady(player1, new ArcboundMouser());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyMouser(mouser);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("+1/+1").doesNotContain("-1/-1");
    }

    private void destroyMouser(Permanent mouser) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, mouser.getId());
    }
}
