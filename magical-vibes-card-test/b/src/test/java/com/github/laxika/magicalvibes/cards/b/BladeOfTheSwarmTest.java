package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.StarfieldVocalist;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeOfTheSwarm.class, StarfieldVocalist.class})
class BladeOfTheSwarmTest extends BaseCardTest {

    @Test
    void putsTwoPlusOneCountersOnItself() {
        castBladeOfTheSwarm();

        harness.handleListChoice(player1, "Put two +1/+1 counters on this creature.");
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Blade of the Swarm");
        assertThat(blade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsTargetWarpCardOnBottomOfItsOwnersLibrary() {
        Card warpCard = new StarfieldVocalist();
        Card nonWarpCard = new BladeOfTheSwarm();
        Card existingLibraryCard = new BladeOfTheSwarm();
        harness.setExile(player2, List.of(nonWarpCard, warpCard));
        harness.setLibrary(player2, List.of(existingLibraryCard));

        castBladeOfTheSwarm();

        harness.handleListChoice(player1,
                "Put target exiled card with warp on the bottom of its owner's library.");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonWarpCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, warpCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonWarpCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingLibraryCard, warpCard);
    }

    @Test
    void cannotChooseExileModeWithoutAnEligibleTarget() {
        harness.setExile(player2, List.of(new BladeOfTheSwarm()));

        castBladeOfTheSwarm();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("Put two +1/+1 counters on this creature.");
        harness.handleListChoice(player1, "Put two +1/+1 counters on this creature.");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blade of the Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void exileModeDoesNothingWhenItsTargetLeavesExileBeforeResolution() {
        Card warpCard = new StarfieldVocalist();
        Card existingLibraryCard = new BladeOfTheSwarm();
        harness.setExile(player1, List.of(warpCard));
        harness.setLibrary(player1, List.of(existingLibraryCard));

        castBladeOfTheSwarm();
        harness.handleListChoice(player1,
                "Put target exiled card with warp on the bottom of its owner's library.");
        harness.handlePermanentChosen(player1, warpCard.getId());
        gd.removeFromExile(warpCard.getId());
        gd.playerHands.get(player1.getId()).add(warpCard);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingLibraryCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(warpCard);
        assertThat(findPermanent(player1, "Blade of the Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBladeOfTheSwarm() {
        harness.setHand(player1, List.of(new BladeOfTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

}
