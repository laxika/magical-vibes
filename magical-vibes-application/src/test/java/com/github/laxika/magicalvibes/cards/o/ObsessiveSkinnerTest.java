package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObsessiveSkinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObsessiveSkinner()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Delirium puts a +1/+1 counter on target creature during an opponent's upkeep")
    void deliriumPutsCounterOnTargetCreatureOnOpponentsUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ObsessiveSkinner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep without delirium")
    void doesNotTriggerWithoutDelirium() {
        addCreatureReady(player1, new ObsessiveSkinner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target a creature controlled by either player")
    void canTargetOwnCreatureOnOpponentsUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ObsessiveSkinner());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player2);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ObsessiveSkinner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rechecks delirium when the upkeep ability resolves")
    void rechecksDeliriumOnResolution() {
        setDelirium();
        addCreatureReady(player1, new ObsessiveSkinner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
    }
}
