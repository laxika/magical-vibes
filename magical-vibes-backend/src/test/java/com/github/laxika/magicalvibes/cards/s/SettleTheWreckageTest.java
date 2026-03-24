package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SettleTheWreckageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Settle the Wreckage has correct SPELL effect")
    void hasCorrectEffect() {
        SettleTheWreckage card = new SettleTheWreckage();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect.class);
    }

    @Test
    @DisplayName("Effect can target a player")
    void effectCanTargetPlayer() {
        SettleTheWreckage card = new SettleTheWreckage();

        assertThat(card.getEffects(EffectSlot.SPELL).getFirst().canTargetPlayer()).isTrue();
    }

    // ===== Exile attacking creatures =====

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
        Permanent attacker = addReadyCreature(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addReadyCreature(player2, new HillGiant()); // not attacking

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // Only the attacker should be exiled
        assertThat(exiledCardsOwnedBy(player2)).hasSize(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    // ===== Library search for basic lands =====

    @Test
    @DisplayName("Target player gets library search for basic lands after exile")
    void targetPlayerGetsLibrarySearch() {
        setupAttackingCreatures(player2, 2);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Only basic lands should be offered
        assertThat(gd.interaction.librarySearch().cards())
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
        gs.handleLibraryCardChosen(gd, player2, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Pick second land
        gs.handleLibraryCardChosen(gd, player2, 0);

        // Both lands should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 2);
        long tappedLandCount = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .count();
        assertThat(tappedLandCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Target player may decline to search (fail to find)")
    void targetPlayerMayDeclineSearch() {
        setupAttackingCreatures(player2, 1);
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Decline search
        gs.handleLibraryCardChosen(gd, player2, -1);

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
        gs.handleLibraryCardChosen(gd, player2, 0); // pick 1
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        gs.handleLibraryCardChosen(gd, player2, 0); // pick 2
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        gs.handleLibraryCardChosen(gd, player2, 0); // pick 3

        // All 3 exiled, 3 lands found
        assertThat(exiledCardsOwnedBy(player2)).hasSize(3);
        long tappedLandCount = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .count();
        assertThat(tappedLandCount).isGreaterThanOrEqualTo(3);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("No attacking creatures — no exile, no search")
    void noAttackingCreaturesNoExileNoSearch() {
        // Add a non-attacking creature
        addReadyCreature(player2, new GrizzlyBears());
        setupLibraryWithBasicLands(player2);

        castSettleTheWreckage(player1, player2);
        harness.passBothPriorities(); // resolve Settle

        // No creatures exiled
        assertThat(exiledCardsOwnedBy(player2)).isEmpty();
        // No library search initiated
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
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
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
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
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
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
        gs.handleLibraryCardChosen(gd, player2, 0);
        gs.handleLibraryCardChosen(gd, player2, -1);

        // Only 1 land should have entered
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
    }

    // ===== Helper methods =====

    private void setupAttackingCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent creature = addReadyCreature(player, new GrizzlyBears());
            creature.setAttacking(true);
        }
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupLibraryWithBasicLands(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new Mountain(), new GrizzlyBears()));
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
