package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GitaxianAnatomist.class, GrizzlyBears.class})
class GitaxianAnatomistTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB trigger may tap it and proliferate")
    void etbMayTapAndProliferate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent anatomist = findPermanent(player1, "Gitaxian Anatomist");
        assertThat(anatomist.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its ETB may ability may be declined")
    void etbMayBeDeclined() {
        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Gitaxian Anatomist").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped Anatomist cannot pay for proliferate")
    void alreadyTappedCannotProliferate() {
        Permanent support = harness.addToBattlefieldAndReturn(player2, new GitaxianAnatomist());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Gitaxian Anatomist").tap();
        harness.passBothPriorities();
        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(support.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Anatomist that left the battlefield cannot pay for proliferate")
    void absentSourceCannotProliferate() {
        Permanent support = harness.addToBattlefieldAndReturn(player2, new GitaxianAnatomist());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent anatomist = findPermanent(player1, "Gitaxian Anatomist");
        gd.playerBattlefields.get(player1.getId()).remove(anatomist);
        gd.playerGraveyards.get(player1.getId()).add(anatomist.getCard());
        harness.passBothPriorities();
        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(support.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can increase opposing permanent counters and player poison")
    void proliferatesOpposingPermanentAndPlayer() {
        Permanent support = harness.addToBattlefieldAndReturn(player2, new GitaxianAnatomist());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(support.getId(), player2.getId()));

        assertThat(support.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(findPermanent(player1, "Gitaxian Anatomist").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Proliferate may choose no permanents even after paying the tap cost")
    void mayChooseNoCounters() {
        Permanent support = harness.addToBattlefieldAndReturn(player2, new GitaxianAnatomist());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GitaxianAnatomist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(support.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Gitaxian Anatomist").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
