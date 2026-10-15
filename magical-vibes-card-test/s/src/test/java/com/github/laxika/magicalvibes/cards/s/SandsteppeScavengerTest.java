package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KolaghanAspirant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandsteppeScavenger.class, KolaghanAspirant.class})
class SandsteppeScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and bolsters the creature with the least toughness")
    void entersAndBolstersLeastToughnessCreature() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());

        castSandsteppeScavenger();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        Permanent first = addCreatureReady(player1, new KolaghanAspirant());
        Permanent second = addCreatureReady(player1, new KolaghanAspirant());

        castSandsteppeScavenger();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bolsters itself when it is the only creature controlled")
    void bolstersItself() {
        castSandsteppeScavenger();

        assertThat(findPermanent(player1, "Sandsteppe Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores an opponent's creature with lower toughness")
    void ignoresOpponentsCreatures() {
        Permanent opponent = addCreatureReady(player2, new KolaghanAspirant());

        castSandsteppeScavenger();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Sandsteppe Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Includes the entering creature in a tie for least toughness")
    void enteringCreatureCanBeChosenInTie() {
        Permanent first = addCreatureReady(player1, new SandsteppeScavenger());

        castSandsteppeScavenger();

        Permanent entering = findPermanents(player1, "Sandsteppe Scavenger").get(1);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), entering.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(entering.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Determines least toughness when the trigger resolves, including existing counters")
    void usesCurrentToughnessAtResolution() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        harness.castFromHand(player1, new SandsteppeScavenger(), "{4}{G}");
        harness.passBothPriorities();
        aspirant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Sandsteppe Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves without a choice when no creatures remain under its controller's control")
    void doesNothingWithoutCreatures() {
        harness.castFromHand(player1, new SandsteppeScavenger(), "{4}{G}");
        harness.passBothPriorities();
        Permanent scavenger = findPermanent(player1, "Sandsteppe Scavenger");
        gd.playerBattlefields.get(player1.getId()).remove(scavenger);
        gd.playerGraveyards.get(player1.getId()).add(scavenger.getCard());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSandsteppeScavenger() {
        harness.castFromHand(player1, new SandsteppeScavenger(), "{4}{G}");
        resolveAllTriggers();
    }
}
