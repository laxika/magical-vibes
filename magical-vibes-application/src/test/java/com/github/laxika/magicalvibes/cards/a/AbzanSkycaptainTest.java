package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AbzanSkycaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Abzan Skycaptain dies, bolster 2 puts counters on the least-tough creature")
    void deathTriggersBolsterTwo() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent leastToughCreature = new Permanent(new GrizzlyBears());
        Permanent largerCreature = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player1.getId())
                .addAll(List.of(leastToughCreature, largerCreature));

        destroySkycaptain();
        harness.passBothPriorities();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(leastToughCreature.getEffectivePower()).isEqualTo(4);
        assertThat(leastToughCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("When creatures are tied for least toughness, bolster 2 lets the controller choose")
    void deathTriggerChoosesAmongTiedCreatures() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent first = new Permanent(new GrizzlyBears());
        Permanent second = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).addAll(List.of(first, second));

        destroySkycaptain();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 2));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroySkycaptain() {
        UUID skycaptainId = harness.getPermanentId(player1, "Abzan Skycaptain");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, skycaptainId);
        harness.passBothPriorities();
    }
}
