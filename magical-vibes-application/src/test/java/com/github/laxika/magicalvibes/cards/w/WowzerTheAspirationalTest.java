package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WowzerTheAspirational.class)
class WowzerTheAspirationalTest extends BaseCardTest {

    @Test
    void winsWhenEveryConditionIsMet() {
        addWowzerAndRequirements();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotWinWithoutTheInitiative() {
        addWowzerAndRequirements();
        gd.initiativePlayerId = null;

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void checksTheInitiativeAgainWhenTheTriggerResolves() {
        addWowzerAndRequirements();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        gd.initiativePlayerId = null;
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    private void addWowzerAndRequirements() {
        addCreatureReady(player1, new WowzerTheAspirational());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        gd.monarchPlayerId = player1.getId();
        gd.initiativePlayerId = player1.getId();
        gd.playersWithCityBlessing.add(player1.getId());

        List.of(CardSubtype.BLOOD, CardSubtype.CLUE, CardSubtype.FOOD, CardSubtype.MAP,
                        CardSubtype.POWERSTONE, CardSubtype.TREASURE)
                .forEach(subtype -> harness.addToBattlefield(player1, token(subtype)));
    }

    private static Card token(CardSubtype subtype) {
        Card card = new Card();
        card.setName(subtype.name());
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
