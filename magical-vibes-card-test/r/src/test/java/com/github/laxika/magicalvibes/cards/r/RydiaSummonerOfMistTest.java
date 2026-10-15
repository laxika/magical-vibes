package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SummonChocoMog;
import com.github.laxika.magicalvibes.cards.t.TravelingChocobo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RydiaSummonerOfMist.class, Forest.class, TravelingChocobo.class, SummonChocoMog.class, DoublingSeason.class})
class RydiaSummonerOfMistTest extends BaseCardTest {

    @Test
    void landfallMayDiscardToDraw() {
        Card discarded = new TravelingChocobo();
        Card drawn = new TravelingChocobo();
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded, new Forest()));

        harness.playLand(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void landfallMayBeDeclined() {
        Card discarded = new TravelingChocobo();
        Card drawn = new TravelingChocobo();
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded, new Forest()));

        harness.playLand(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
    }

    @Test
    void returnsSagaWithFinalityAndHasteForItsManaValue() {
        Permanent rydia = addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returnedSaga = findPermanent(player1, "Summon: Choco/Mog");
        assertThat(returnedSaga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returnedSaga, Keyword.HASTE)).isTrue();
        assertThat(rydia.isTapped()).isTrue();
    }

    @Test
    void cannotReturnNonSagaCard() {
        addRydiaReady();
        Card creature = new TravelingChocobo();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summonAbilityRequiresSorcerySpeed() {
        addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landfallDrawsBeforePlayersReceivePriorityAfterDiscard() {
        Card discarded = new TravelingChocobo();
        Card drawn = new TravelingChocobo();
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded, new Forest()));

        harness.playLand(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landfallWithEmptyHandDoesNotDraw() {
        Card drawn = new TravelingChocobo();
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        harness.setHand(player1, List.of(new TravelingChocobo()));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnedSagaLosesHasteAtCleanupButKeepsFinality() {
        addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();
        Permanent returnedSaga = findPermanent(player1, "Summon: Choco/Mog");
        assertThat(gqs.hasKeyword(gd, returnedSaga, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, returnedSaga, Keyword.HASTE)).isFalse();
        assertThat(returnedSaga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Summon: Choco/Mog");
    }

    @Test
    void cannotReturnSagaWithDifferentManaValueThanX() {
        addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 4, saga.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnSagaFromOpponentsGraveyard() {
        addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player2, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summonAbilityCannotUseSummoningSickRydia() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RydiaSummonerOfMist());
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void finalityCounterIsDoubledByCounterReplacement() {
        addRydiaReady();
        harness.addToBattlefield(player1, new DoublingSeason());
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        Permanent returnedSaga = findPermanent(player1, "Summon: Choco/Mog");
        assertThat(returnedSaga.getCounterCount(CounterType.FINALITY)).isEqualTo(2);
    }

    @Test
    void summonDoesNotReturnTargetThatLeftGraveyardBeforeResolution() {
        Permanent rydia = addRydiaReady();
        Card saga = new SummonChocoMog();
        harness.setGraveyard(player1, List.of(saga));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 3, saga.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Summon: Choco/Mog");
        assertThat(rydia.isTapped()).isTrue();
    }

    private Permanent addRydiaReady() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return addCreatureReady(player1, new RydiaSummonerOfMist());
    }
}
