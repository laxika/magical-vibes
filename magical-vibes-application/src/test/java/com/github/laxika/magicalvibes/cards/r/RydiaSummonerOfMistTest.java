package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SummonAlexander;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RydiaSummonerOfMist.class, Forest.class, GrizzlyBears.class, SummonAlexander.class})
class RydiaSummonerOfMistTest extends BaseCardTest {

    @Test
    void landfallMayDiscardToDraw() {
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
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
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
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
        Card saga = new SummonAlexander();
        harness.setGraveyard(player1, List.of(saga));

        harness.activateAbility(player1, 0, 0, 0, saga.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returnedSaga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SummonAlexander)
                .findFirst()
                .orElseThrow();
        assertThat(returnedSaga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(returnedSaga.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(rydia.isTapped()).isTrue();
    }

    @Test
    void cannotReturnNonSagaCard() {
        addRydiaReady();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summonAbilityRequiresSorcerySpeed() {
        addRydiaReady();
        Card saga = new SummonAlexander();
        harness.setGraveyard(player1, List.of(saga));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, saga.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addRydiaReady() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return addCreatureReady(player1, new RydiaSummonerOfMist());
    }
}
