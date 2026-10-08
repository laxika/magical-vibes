package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({VatEmergence.class, CopperLonglegs.class, Duress.class})
class VatEmergenceTest extends BaseCardTest {

    @Test
    void returnsCreatureFromAnyGraveyardAndProliferates() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        markedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card creature = new CopperLonglegs();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(markedCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreatureCard() {
        Card sorcery = new Duress();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        markedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void proliferatesEachExistingCounterKindOnChosenOpponentPermanentAndPlayer() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        markedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        markedCreature.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosenCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        unchosenCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(markedCreature.getId(), player2.getId()));

        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(markedCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayChooseNothingToProliferateAfterReturningCreature() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        markedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsCreatureWhenThereAreNoCountersToProliferate() {
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VatEmergence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId())
                        && permanent.getCounters().isEmpty() && !permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.stack).isEmpty();
    }
}
