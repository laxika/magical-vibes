package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporogenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may put a fungus counter on a nontoken creature")
    void upkeepPutsCounterOnChosenNontokenCreature() {
        harness.addToBattlefield(player1, new Sporogenesis());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature());

        advanceToUpkeep(player1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(token.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with fungus counters creates one Saproling per fungus counter when it dies")
    void deathCreatesSaprolingsForFungusCountersOnly() {
        harness.addToBattlefield(player1, new Sporogenesis());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.FUNGUS, 2);
        creature.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Leaving the battlefield removes fungus counters from creatures")
    void leavesRemovesFungusCountersFromCreatures() {
        Permanent sporogenesis = harness.addToBattlefieldAndReturn(player1, new Sporogenesis());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownCreature.setCounterCount(CounterType.FUNGUS, 1);
        opposingCreature.setCounterCount(CounterType.FUNGUS, 2);
        land.setCounterCount(CounterType.FUNGUS, 3);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, sporogenesis.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(land.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
    }

    private Card createTokenCreature() {
        Card token = new Card();
        token.setName("Saproling Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setToken(true);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setSubtypes(List.of(CardSubtype.SAPROLING));
        return token;
    }
}
