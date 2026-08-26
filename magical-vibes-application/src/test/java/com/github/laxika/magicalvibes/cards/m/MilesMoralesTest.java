package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PeterParker;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilesMorales.class, UltimateSpiderMan.class, GrizzlyBears.class, PeterParker.class, WoollySpider.class})
class MilesMoralesTest extends BaseCardTest {

    @Test
    @DisplayName("Miles Morales puts counters on up to two creatures when entering")
    void putsCountersOnUpToTwoCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilesMorales()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Miles Morales transforms at sorcery speed")
    void transformsAtSorcerySpeed() {
        Permanent miles = addCreatureReady(player1, new MilesMorales());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(miles.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Camouflage adds a counter and grants temporary hexproof and colorlessness")
    void camouflage() {
        Permanent ultimateSpiderMan = addBackReady(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ultimateSpiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ultimateSpiderMan, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, ultimateSpiderMan)).isEmpty();

        endTurn();

        assertThat(gqs.hasKeyword(gd, ultimateSpiderMan, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, ultimateSpiderMan)).isNotEmpty();
    }

    @Test
    @DisplayName("Ultimate Spider-Man doubles every counter kind on controlled Spiders and legendary creatures")
    void doublesCountersOnSpidersAndLegendsYouControl() {
        Permanent ultimateSpiderMan = addBackReady(player1);
        ultimateSpiderMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ultimateSpiderMan.setCounterCount(CounterType.CHARGE, 1);

        Permanent spider = addCreatureReady(player1, new WoollySpider());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        spider.setCounterCount(CounterType.CHARGE, 2);

        Permanent legendaryCreature = addCreatureReady(player1, new PeterParker());
        legendaryCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());
        ordinaryCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent opponentLegendary = addCreatureReady(player2, new PeterParker());
        opponentLegendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ultimateSpiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ultimateSpiderMan.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(spider.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(legendaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentLegendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent addBackReady(Player player) {
        MilesMorales card = new MilesMorales();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
