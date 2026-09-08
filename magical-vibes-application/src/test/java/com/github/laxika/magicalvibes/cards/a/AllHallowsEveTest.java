package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllHallowsEve.class, GrizzlyBears.class})
class AllHallowsEveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself with two scream counters")
    void exilesWithTwoScreamCounters() {
        AllHallowsEve card = new AllHallowsEve();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Removes one scream counter during its owner's upkeep only")
    void removesOneScreamCounterDuringOwnersUpkeep() {
        AllHallowsEve card = exileWithScreamCounters(2);

        triggerUpkeep(player2);
        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 2);

        triggerUpkeep(player1);
        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Does nothing if it leaves exile before its upkeep trigger resolves")
    void doesNothingIfItLeavesExileBeforeTriggerResolves() {
        AllHallowsEve card = exileWithScreamCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        gd.removeFromExile(card.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardScreamCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Returns all creature cards after its last scream counter is removed")
    void returnsAllCreatureCardsAfterLastCounterIsRemoved() {
        AllHallowsEve card = exileWithScreamCounters(1);
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        Card unrelated = new AllHallowsEve();
        harness.setGraveyard(player1, List.of(player1Creature, unrelated));
        harness.setGraveyard(player2, List.of(player2Creature));

        triggerUpkeep(player1);

        assertThat(gd.exiledCardScreamCounters).doesNotContainKey(card.getId());
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(player1Creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(player2Creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(card, unrelated)
                .doesNotContain(player1Creature);
    }

    private AllHallowsEve exileWithScreamCounters(int counters) {
        AllHallowsEve card = new AllHallowsEve();
        gd.addToExile(player1.getId(), card);
        gd.exiledCardScreamCounters.put(card.getId(), counters);
        return card;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
