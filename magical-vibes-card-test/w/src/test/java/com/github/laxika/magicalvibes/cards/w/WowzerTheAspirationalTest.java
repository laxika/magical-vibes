package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LilianasOtherContract;
import com.github.laxika.magicalvibes.cards.l.LilianasUndeadMinion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WowzerTheAspirational.class, LilianasOtherContract.class, LilianasUndeadMinion.class})
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

    @ParameterizedTest
    @ValueSource(strings = {"ENERGY", "BLOOD", "CLUE", "FOOD", "MAP", "POWERSTONE",
            "TREASURE", "MONARCH", "CITY_BLESSING", "INITIATIVE"})
    void doesNotTriggerWhenAnyRequirementIsMissing(String requirement) {
        addWowzerAndRequirements();
        removeRequirement(requirement);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENERGY", "BLOOD", "CLUE", "FOOD", "MAP", "POWERSTONE",
            "TREASURE", "MONARCH", "INITIATIVE"})
    void doesNotWinWhenARequirementIsLostBeforeResolution(String requirement) {
        addWowzerAndRequirements();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);

        removeRequirement(requirement);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void gainingTheInitiativeAfterAttackingDoesNotCreateATrigger() {
        addWowzerAndRequirements();
        gd.initiativePlayerId = player2.getId();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();

        gd.initiativePlayerId = player1.getId();
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void anOpponentsTreasureDoesNotMeetTheRequirement() {
        addWowzerAndRequirements();
        removeRequirement("TREASURE");
        harness.addToBattlefield(player2, token(CardSubtype.TREASURE));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void stillWinsIfWowzerLeavesTheBattlefieldBeforeResolution() {
        addWowzerAndRequirements();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void winsEvenWhenTheOpponentCannotLoseTheGame() {
        addWowzerAndRequirements();
        LilianasOtherContract contract = new LilianasOtherContract();
        Permanent minion = harness.addToBattlefieldAndReturn(player2, contract);
        minion.setCard(contract.getBackFaceCard());
        minion.setTransformed(true);
        minion.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private void removeRequirement(String requirement) {
        switch (requirement) {
            case "ENERGY" -> gd.playerEnergyCounters.put(player1.getId(), 0);
            case "MONARCH" -> gd.monarchPlayerId = player2.getId();
            case "CITY_BLESSING" -> gd.playersWithCityBlessing.remove(player1.getId());
            case "INITIATIVE" -> gd.initiativePlayerId = player2.getId();
            default -> gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                    permanent.getCard().getSubtypes().contains(CardSubtype.valueOf(requirement)));
        }
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
        card.setToken(true);
        return card;
    }
}
