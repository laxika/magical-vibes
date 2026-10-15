package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.i.InfectiousBite;
import com.github.laxika.magicalvibes.cards.m.MazeSkullbomb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnaturalRestoration.class, CopperLonglegs.class, InfectiousBite.class, MazeSkullbomb.class})
class UnnaturalRestorationTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentCardToHandAndProliferates() {
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetNonpermanentCard() {
        Card instant = new InfectiousBite();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCardInOpponentGraveyard() {
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNoncreaturePermanentWhenThereAreNoCounters() {
        Card artifact = new MazeSkullbomb();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unnatural Restoration");
    }

    @Test
    void mayChooseNoPermanentsOrPlayersToProliferate() {
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatesEveryExistingCounterKindOnSelectedOpponentPermanentAndPlayer() {
        Card artifact = new MazeSkullbomb();
        harness.setGraveyard(player1, List.of(artifact));
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        selected.setCounterCount(CounterType.OIL, 2);
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(selected.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void doesNotProliferateWhenGraveyardTargetIsRemovedBeforeResolution() {
        Card creature = new CopperLonglegs();
        harness.setGraveyard(player1, List.of(creature));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new UnnaturalRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unnatural Restoration");
    }
}
