package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AmprynTactician;
import com.github.laxika.magicalvibes.cards.v.VeteransSidearm;
import com.github.laxika.magicalvibes.cards.g.GuardiansOfMeletis;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicSeeker.class, VeteransSidearm.class, AmprynTactician.class, GuardiansOfMeletis.class})
class RelicSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming renowned offers the Equipment search, which puts the chosen card into hand")
    void becomingRenownedFindsEquipment() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        setupLibrary();

        attackUnblocked();

        GameData gd = harness.getGameData();
        assertThat(seeker.isRenowned()).isTrue();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.EQUIPMENT));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningSkipsSearch() {
        addCreatureReady(player1, new RelicSeeker());
        setupLibrary();

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An already renowned Relic Seeker does not trigger again")
    void alreadyRenownedDoesNotTrigger() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        seeker.setRenowned(true);
        setupLibrary();

        attackUnblocked();

        GameData gd = harness.getGameData();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked Relic Seeker never becomes renowned, so nothing triggers")
    void blockedDoesNotTrigger() {
        Permanent seeker = addCreatureReady(player1, new RelicSeeker());
        addCreatureReady(player2, new GuardiansOfMeletis());
        setupLibrary();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(seeker.isRenowned()).isFalse();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("Searching with no Equipment leaves the hand unchanged and shuffles")
    void noEquipmentStillShuffles() {
        addCreatureReady(player1, new RelicSeeker());
        AmprynTactician creature = new AmprynTactician();
        harness.setLibrary(player1, List.of(creature));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A restricted Equipment search may fail to find even with Equipment available")
    void mayFailToFindEquipment() {
        addCreatureReady(player1, new RelicSeeker());
        VeteransSidearm equipment = new VeteransSidearm();
        harness.setLibrary(player1, List.of(equipment));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The opposing controller searches their own library and receives the revealed Equipment")
    void opposingControllerSearchesOwnLibrary() {
        Permanent seeker = addCreatureReady(player2, new RelicSeeker());
        VeteransSidearm equipment = new VeteransSidearm();
        AmprynTactician otherLibraryCard = new AmprynTactician();
        harness.setLibrary(player2, List.of(equipment));
        harness.setLibrary(player1, List.of(otherLibraryCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(seeker.isRenowned()).isTrue();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).contains(equipment);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
        assertThat(gameLogContains("reveals Veteran's Sidearm")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }
    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new VeteransSidearm(), new AmprynTactician()));
    }
}
