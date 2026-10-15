package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SettleTheWreckage.class, GrizzlyBears.class, HillGiant.class,
        Plains.class, Forest.class, Island.class, Mountain.class, TreetopVillage.class})
class SettleTheWreckageTest extends BaseCardTest {


    @Test
    @DisplayName("Exiles all attacking creatures target player controls")
    void exilesAllAttackingCreatures() {
        setupAttackingCreatures(player2, 2);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Both attacking creatures should be exiled
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count()).isZero();
        assertThat(exiledCardsOwnedBy(player2)).hasSize(2);
    }

    @Test
    @DisplayName("Does not exile non-attacking creatures")
    void doesNotExileNonAttackingCreatures() {
        // Add one attacking and one non-attacking creature
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HillGiant()); // not attacking

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Only the attacker should be exiled
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }


    @Test
    @DisplayName("Target player gets library search for basic lands after exile")
    void targetPlayerGetsLibrarySearch() {
        setupAttackingCreatures(player2, 2);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Only basic lands should be offered
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Picking basic lands puts them onto the battlefield tapped")
    void pickedLandsEnterBattlefieldTapped() {
        setupAttackingCreatures(player2, 2);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Pick first land
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Pick second land
        harness.handleCardChosen(player2, 0);

        // Both lands should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 2);
        long tappedLandCount = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .count();
        assertThat(tappedLandCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Target player may fail to find any basic lands")
    void targetPlayerMayFailToFind() {
        setupAttackingCreatures(player2, 1);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Fail to find a basic land
        harness.handleCardChosen(player2, -1);

        // No lands entered
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Search count matches number of creatures exiled")
    void searchCountMatchesExiledCreatureCount() {
        setupAttackingCreatures(player2, 3);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Should be able to pick 3 lands (one per exiled creature)
        harness.handleCardChosen(player2, 0); // pick 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0); // pick 2
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0); // pick 3

        // All 3 exiled, 3 lands found
        assertThat(exiledCardsOwnedBy(player2)).hasSize(3);
        long tappedLandCount = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .count();
        assertThat(tappedLandCount).isGreaterThanOrEqualTo(3);
    }


    @Test
    @DisplayName("No attacking creatures still permits searching for zero lands and shuffling")
    void noAttackingCreaturesStillPermitsSearch() {
        // Add a non-attacking creature
        addCreatureReady(player2, new GrizzlyBears());
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // No creatures exiled
        assertThat(exiledCardsOwnedBy(player2)).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No basic lands in library — search resolves with no results")
    void noBasicLandsInLibrary() {
        setupAttackingCreatures(player2, 1);

        // Library with only non-basic cards
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Creature is exiled
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
        // No search prompt since no basic lands
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Empty library — search resolves with shuffle message")
    void emptyLibrary() {
        setupAttackingCreatures(player2, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Creature is exiled
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
        // No search prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Can partially search — pick some lands then fail to find")
    void partialSearch() {
        setupAttackingCreatures(player2, 3);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Pick 1 land, then decline
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        // Only 1 land should have entered
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
    }


    @Test
    @DisplayName("Exiles an attacking animated land and grants a basic land search")
    void exilesAttackingAnimatedLand() {
        Permanent village = addCreatureReady(player2, new TreetopVillage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        village.setAttacking(true);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(village);
        assertThat(exiledCardsOwnedBy(player2)).extracting(e -> e.card().getName())
                .containsExactly("Treetop Village");
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Declining the optional search leaves the library unchanged")
    void mayDeclineSearchWithoutShuffling() {
        setupAttackingCreatures(player2, 1);
        setupLibraryWithBasicLands(player2);
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalLibrary);
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library") || entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Can exile the caster's own attacking creatures")
    void canTargetCaster() {
        setupAttackingCreatures(player1, 1);
        setupLibraryWithBasicLands(player1);
        Permanent opposingAttacker = addCreatureReady(player2, new GrizzlyBears());
        opposingAttacker.setAttacking(true);

        castSettleTheWreckage(player1, player1);
        harness.passBothPriorities();

        assertThat(exiledCardsOwnedBy(player1)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingAttacker);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Searching without matching basic lands still counts as searching")
    void searchWithoutMatchingBasicLandsCountsAsSearch() {
        setupAttackingCreatures(player2, 1);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new HillGiant()));

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player2.getId());
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
    }

    private void setupAttackingCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent creature = addCreatureReady(player, new GrizzlyBears());
            creature.setAttacking(true);
        }
    }

    private void setupLibraryWithBasicLands(com.github.laxika.magicalvibes.model.Player player) {
        harness.setLibrary(player,
                List.of(new Plains(), new Forest(), new Island(), new Mountain(), new GrizzlyBears()));
    }

    private void castSettleTheWreckage(com.github.laxika.magicalvibes.model.Player caster,
                                       com.github.laxika.magicalvibes.model.Player target) {
        harness.setHand(caster, List.of(new SettleTheWreckage()));
        harness.addMana(caster, ManaColor.WHITE, 4);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castInstant(caster, 0, target.getId());
    }

    private List<ExiledCardEntry> exiledCardsOwnedBy(com.github.laxika.magicalvibes.model.Player player) {
        return gd.exiledCards.stream()
                .filter(e -> e.ownerId().equals(player.getId()))
                .toList();
    }
}
