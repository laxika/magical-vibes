package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtherworldlyJourney.class, IsamaruHoundOfKonda.class, Island.class})
class OtherworldlyJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and returns it at the next end step with a +1/+1 counter")
    void returnsCreatureWithCounterAtNextEndStep() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player1, "Isamaru, Hound of Konda");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Isamaru, Hound of Konda");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("Returns an opponent's creature under its owner's control with a +1/+1 counter")
    void returnsOpponentsCreatureToItsOwner() {
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));

        advanceToEndStep();

        Permanent returned = findPermanent(player2, "Isamaru, Hound of Konda");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
