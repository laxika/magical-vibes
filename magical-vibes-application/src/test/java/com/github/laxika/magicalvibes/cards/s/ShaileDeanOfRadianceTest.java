package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmbroseDeanOfShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaileDeanOfRadiance.class, EmbroseDeanOfShadow.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, WrathOfGod.class})
class ShaileDeanOfRadianceTest extends BaseCardTest {

    @Test
    void shailePutsCountersOnControlledCreaturesThatEnteredThisTurn() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent shaile = gd.playerBattlefields.get(player1.getId()).getFirst();
        shaile.setSummoningSick(false);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent recentCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void embroseCanBeCastAsTheBackFaceAndDamagesTheCreatureItCounters() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent embrose = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EmbroseDeanOfShadow)
                .findFirst()
                .orElseThrow();
        embrose.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void embroseCannotTargetItself() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent embrose = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EmbroseDeanOfShadow)
                .findFirst()
                .orElseThrow();
        embrose.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, embrose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void embroseDrawsWhenAControlledCreatureWithACounterDies() {
        addReadyEmbrose();
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    private Permanent addReadyShaile() {
        Permanent shaile = addCreatureReady(player1, new ShaileDeanOfRadiance());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shaile;
    }

    private Permanent addReadyEmbrose() {
        ShaileDeanOfRadiance card = new ShaileDeanOfRadiance();
        Permanent embrose = harness.addToBattlefieldAndReturn(player1, card);
        embrose.setCard(card.getBackFaceCard());
        embrose.setTransformed(true);
        embrose.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return embrose;
    }
}
