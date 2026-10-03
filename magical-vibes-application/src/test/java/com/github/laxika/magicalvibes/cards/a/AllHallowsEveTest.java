package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HeadlessHorseman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllHallowsEve.class, HeadlessHorseman.class})
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

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Does nothing if it leaves exile before its upkeep trigger resolves")
    void doesNothingIfItLeavesExileBeforeTriggerResolves() {
        AllHallowsEve card = exileWithScreamCounters(1);

        advanceToUpkeep(player1);
        gd.removeFromExile(card.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardScreamCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Returns all creature cards after its last scream counter is removed")
    void returnsAllCreatureCardsAfterLastCounterIsRemoved() {
        AllHallowsEve card = exileWithScreamCounters(1);
        Card player1Creature = new HeadlessHorseman();
        Card player2Creature = new HeadlessHorseman();
        Card unrelated = new AllHallowsEve();
        harness.setGraveyard(player1, List.of(player1Creature, unrelated));
        harness.setGraveyard(player2, List.of(player2Creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

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

    @Test
    @DisplayName("A resolved spell returns every creature only on its second upkeep")
    void returnsCreaturesOnSecondUpkeepAfterCasting() {
        AllHallowsEve card = new AllHallowsEve();
        Card firstCreature = new HeadlessHorseman();
        Card secondCreature = new HeadlessHorseman();
        Card opposingCreature = new HeadlessHorseman();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardScreamCounters).containsEntry(card.getId(), 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
        harness.assertNotOnBattlefield(player1, "Headless Horseman");
        harness.assertNotOnBattlefield(player2, "Headless Horseman");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opposingCreature.getId());
    }

    @Test
    @DisplayName("An upkeep trigger does nothing if no scream counters remain when it resolves")
    void doesNothingIfCountersDisappearBeforeResolution() {
        AllHallowsEve card = exileWithScreamCounters(1);
        Card creature = new HeadlessHorseman();
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        gd.exiledCardScreamCounters.remove(card.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Headless Horseman");
    }

    @Test
    @DisplayName("The last scream counter still sends the spell to the graveyard when no creatures exist")
    void returnsSpellToGraveyardWithEmptyGraveyards() {
        AllHallowsEve card = exileWithScreamCounters(1);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exiledCardScreamCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private AllHallowsEve exileWithScreamCounters(int counters) {
        AllHallowsEve card = new AllHallowsEve();
        gd.addToExile(player1.getId(), card);
        gd.exiledCardScreamCounters.put(card.getId(), counters);
        return card;
    }

}
