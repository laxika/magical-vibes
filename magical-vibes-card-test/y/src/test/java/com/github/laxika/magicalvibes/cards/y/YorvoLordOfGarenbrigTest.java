package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YorvoLordOfGarenbrig.class, GrizzlyBears.class, ColossalDreadmaw.class,
        WoollyThoctar.class, HillGiant.class, GiantGrowth.class, Unsummon.class})
class YorvoLordOfGarenbrigTest extends BaseCardTest {

    @Test
    void entersWithFourCountersAndDoesNotTriggerForItself() {
        Permanent yorvo = castYorvo();

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void putsOneCounterWhenAnotherGreenCreatureEnters() {
        Permanent yorvo = castYorvo();

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.GREEN);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void putsAnotherCounterWhenEnteringCreatureIsLargerAfterFirstCounter() {
        Permanent yorvo = castYorvo();

        castCreature(new ColossalDreadmaw(), ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN,
                ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void comparesPowerAfterPuttingTheFirstCounterOnYorvo() {
        Permanent yorvo = castYorvo();

        castCreature(new WoollyThoctar(), ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void ignoresNonGreenCreatures() {
        Permanent yorvo = castYorvo();

        castCreature(new HillGiant(), ManaColor.RED, ManaColor.RED, ManaColor.RED, ManaColor.RED);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castYorvo() {
        castCreature(new YorvoLordOfGarenbrig(), ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN);
        return findPermanent(player1, "Yorvo, Lord of Garenbrig");
    }

    @Test
    void comparesTheEnteringCreaturesCurrentPowerAtResolution() {
        Permanent yorvo = castYorvo();
        Permanent entering = castThoctarLeavingYorvoTriggerPending();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, entering.getId());
        resolveAllTriggers();

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void ignoresGreenCreaturesEnteringUnderAnOpponentsControl() {
        Permanent yorvo = castYorvo();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void comparesYorvosCurrentPowerAtResolution() {
        Permanent yorvo = castYorvo();
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, yorvo.getId());
        resolveAllTriggers();

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void usesLastKnownPowerAfterTheEnteringCreatureIsBoostedAndReturnedToHand() {
        Permanent yorvo = castYorvo();
        Permanent entering = castThoctarLeavingYorvoTriggerPending();

        harness.setHand(player1, List.of(new GiantGrowth(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.assertNotOnBattlefield(player1, "Woolly Thoctar");
        resolveAllTriggers();

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent castThoctarLeavingYorvoTriggerPending() {
        harness.setHand(player1, List.of(new WoollyThoctar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        return findPermanent(player1, "Woolly Thoctar");
    }

    private void castCreature(Card creature, ManaColor... mana) {
        harness.setHand(player1, List.of(creature));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
