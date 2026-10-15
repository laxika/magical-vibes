package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EsikasChariot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFirstIroanGames.class, Forest.class, GrizzlyBears.class, EsikasChariot.class})
class TheFirstIroanGamesTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 1/1 white Human Soldier")
    void chapterICreatesHumanSoldier() {
        addSagaWithLore(0);

        advanceToNextChapter();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Human Soldier"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Chapter II puts three +1/+1 counters on a creature you control")
    void chapterIIPutsCountersOnControlledCreature() {
        addSagaWithLore(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III draws two cards when you control a creature with power 4 or greater")
    void chapterIIIDrawsWithPowerFourCreature() {
        addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter III does nothing without a creature with power 4 or greater")
    void chapterIIIDoesNothingWithoutPowerFourCreature() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter IV creates a Gold artifact token")
    void chapterIVCreatesGoldToken() {
        addSagaWithLore(3);

        advanceToNextChapter();

        Permanent gold = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gold"))
                .findFirst()
                .orElseThrow();
        assertThat(gold.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gold.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III does not count an uncrewed Vehicle with printed power four")
    void chapterIIIDoesNotCountUncrewedVehicle() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new EsikasChariot());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III does not count an opponent's power four creature")
    void chapterIIIDoesNotCountOpposingCreature() {
        addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III checks power at resolution when a creature gains power in response")
    void chapterIIIChecksIncreasedPowerAtResolution() {
        addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter III checks power at resolution when a creature loses power in response")
    void chapterIIIChecksDecreasedPowerAtResolution() {
        addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Gold token can be sacrificed for mana immediately")
    void tappedGoldCanProduceManaWithoutUsingStack() {
        addSagaWithLore(3);
        advanceToNextChapter();
        Permanent gold = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gold"))
                .findFirst().orElseThrow();
        gold.tap();
        gold.setSummoningSick(true);
        int goldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gold);

        harness.activateAbility(player1, goldIndex, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Gold");
        harness.assertNotOnBattlefield(player1, "The First Iroan Games");
        harness.assertInGraveyard(player1, "The First Iroan Games");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting the Saga creates its chapter I token on entry")
    void chapterITriggersOnEntry() {
        harness.castFromHand(player1, new TheFirstIroanGames(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The First Iroan Games");
        harness.assertOnBattlefield(player1, "Human Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Human Soldier")))
                .hasSize(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstIroanGames());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
