package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BurnTheImpure;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgePool.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        Mountain.class, Shock.class, BurnTheImpure.class, Vivisection.class, Cancel.class, SteelSabotage.class})
class KnowledgePoolTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles top 3 cards from each player's library")
    void etbExilesTopThreeFromEachPlayer() {
        // Setup: give each player known cards in their library
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        // Cast Knowledge Pool (costs {6})
        harness.setHand(player1, List.of(new KnowledgePool()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);

        // Resolve artifact spell → puts KP on battlefield, ETB trigger goes on stack
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knowledge Pool");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve ETB trigger
        harness.passBothPriorities();

        // Each player should have 3 fewer cards in their library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);

        // Cards should be in the KP's permanentExiledCards pool
        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        List<Card> pool = gd.getCardsExiledByPermanent(kpPermId);
        assertThat(pool).hasSize(6); // 3 from each player

        // Cards should also be in each player's exile zone
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB exiles fewer cards when library has less than 3")
    void etbExilesFewerWhenLibrarySmall() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Mountain()));

        harness.setHand(player1, List.of(new KnowledgePool()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        assertThat(gd.getCardsExiledByPermanent(kpPermId)).hasSize(3); // 2 + 1
    }

    @Test
    @DisplayName("Casting a spell from hand triggers Knowledge Pool")
    void castFromHandTriggersKP() {
        setupKnowledgePoolWithPool();

        // Cast a spell from hand
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Stack should have: original sorcery + KP trigger on top
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getEffectsToResolve().getFirst())
                .isInstanceOf(KnowledgePoolExileAndCastEffect.class);
    }

    @Test
    @DisplayName("Resolving KP trigger exiles original spell and presents choice")
    void resolvingTriggerExilesOriginalAndPresentsChoice() {
        setupKnowledgePoolWithPool();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Resolve KP trigger (on top of stack)
        harness.passBothPriorities();

        // Original spell should be removed from stack and added to KP pool
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Counsel of the Soratami"));

        // Player should be presented with a choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.KnowledgePoolCastChoice.class);
    }

    @Test
    @DisplayName("Player can cast a nonland non-targeted card from the pool without paying mana cost")
    void playerCastsFromPool() {
        // Put a non-targeted creature in the pool
        Card bears = new GrizzlyBears();
        setupKnowledgePoolManually(List.of(bears));

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose the Grizzly Bears from the pool
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // The chosen card should be on the stack
        assertThat(gd.stack).anyMatch(se -> se.getCard().getId().equals(bears.getId()));

        // The chosen card should no longer be in the KP pool
        List<Card> pool = gd.getCardsExiledByPermanent(kpPermId);
        assertThat(pool).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Player can decline to cast from the pool")
    void playerDeclinesFromPool() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        int poolSizeBefore = gd.getCardsExiledByPermanent(kpPermId).size();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Decline by passing empty list
        harness.handleMultipleCardsChosen(player1, List.of());

        // Pool should have grown by 1 (the exiled original spell)
        assertThat(gd.getCardsExiledByPermanent(kpPermId)).hasSize(poolSizeBefore + 1);

        // Interaction should be cleared
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Replacement spell cast from KP does NOT re-trigger Knowledge Pool")
    void replacementSpellDoesNotRetrigger() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        List<Card> pool = gd.getCardsExiledByPermanent(kpPermId);

        Card nonlandCard = pool.stream()
                .filter(c -> !c.hasType(CardType.LAND))
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose a card from the pool
        harness.handleMultipleCardsChosen(player1, List.of(nonlandCard.getId()));

        // The replacement spell should be on the stack, but NO new KP trigger
        // (KP trigger only fires for cast-from-hand)
        long kpTriggers = gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> se.getEffectsToResolve().stream()
                        .anyMatch(e -> e instanceof KnowledgePoolExileAndCastEffect))
                .count();
        assertThat(kpTriggers).isZero();
    }

    @Test
    @DisplayName("If original spell is countered before KP trigger resolves, 'if the player does' fails")
    void originalSpellGoneBeforeTriggerResolves() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        int poolSizeBefore = gd.getCardsExiledByPermanent(kpPermId).size();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Manually remove original spell from stack (simulating it being countered)
        gd.stack.removeIf(se -> se.getCard().getName().equals("Counsel of the Soratami"));

        // Now resolve the KP trigger
        harness.passBothPriorities();

        // No choice should be presented (original spell gone)
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Pool should be unchanged
        assertThat(gd.getCardsExiledByPermanent(kpPermId)).hasSize(poolSizeBefore);
    }

    @Test
    @DisplayName("Lands in the pool are not offered as choices")
    void landsNotOfferedAsChoices() {
        // Put KP on battlefield directly and manually set up pool with only lands
        setupKnowledgePoolManually(List.of(new Forest(), new Mountain()));

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // The original spell (Counsel) gets added to pool but the "other" filter
        // removes the just-exiled card, and only lands remain eligible → no choice
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The just-exiled card is not offered as a choice")
    void justExiledCardNotOffered() {
        // Pool with one nonland card (Shock)
        Card shockInPool = new Shock();
        setupKnowledgePoolManually(List.of(shockInPool));

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Player should be offered only the Shock (not the just-exiled Counsel)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.KnowledgePoolCastChoice.class);

        var validIds = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.KnowledgePoolCastChoice.class).validCardIds();
        assertThat(validIds).contains(shockInPool.getId());

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        // The just-exiled Counsel should NOT be in the valid choices
        Card exiledCounsel = gd.getCardsExiledByPermanent(kpPermId).stream()
                .filter(c -> c.getName().equals("Counsel of the Soratami"))
                .findFirst().orElse(null);
        if (exiledCounsel != null) {
            assertThat(validIds).doesNotContain(exiledCounsel.getId());
        }
    }

    @Test
    @DisplayName("KP trigger still exiles and offers a spell after Knowledge Pool leaves")
    void kpRemovedBeforeTriggerResolves() {
        setupKnowledgePoolWithPool();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Remove KP from battlefield before resolving trigger
        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(kpPermId));

        // Resolve KP trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.KnowledgePoolCastChoice.class);
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Counsel of the Soratami"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Counsel of the Soratami"));

        Card bears = gd.getCardsExiledByPermanent(kpPermId).stream()
                .filter(card -> card instanceof GrizzlyBears).findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a creature from the KP pool puts it on the stack and enters battlefield")
    void creatureFromPool() {
        Card bears = new GrizzlyBears();
        setupKnowledgePoolManually(List.of(bears));

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose the Grizzly Bears
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Bears should be on the stack as a creature spell
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getId().equals(bears.getId())
                        && se.getEntryType() == StackEntryType.CREATURE_SPELL);

        // Resolve it
        harness.passBothPriorities();

        // Bears should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a targeted spell from pool prompts for target selection")
    void targetedSpellFromPool() {
        Card shock = new Shock();
        setupKnowledgePoolManually(List.of(shock));

        // Give player2 a creature to target
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose Shock from pool
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        // Should be waiting for target selection (permanent choice)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose target — Grizzly Bears
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock should now be on the stack targeting bears
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getId().equals(shock.getId())
                        && se.getTargetId().equals(bearsId));

        // Resolve the Shock
        harness.passBothPriorities();

        // Bears should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A counterspell from the pool chooses a spell already on the stack")
    void counterspellFromPoolCanTargetSpellOnStack() {
        Card counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        Card cancel = new Cancel();
        setupKnowledgePoolManually(List.of(cancel));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(cancel.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(counsel.getId());
        harness.handlePermanentChosen(player1, counsel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A modal spell from the pool chooses its mode and target before resolving")
    void modalSpellFromPoolChoosesModeAndTargetWhileCasting() {
        Card sabotage = new SteelSabotage();
        setupKnowledgePoolManually(List.of(sabotage));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sabotage.getId()));

        // The only legal mode returns the Pool itself; the caster must make that choice
        // while casting, before anyone can respond to the replacement spell.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("A spell with an unpaid mandatory sacrifice cost cannot be cast from the pool")
    void mandatoryAdditionalCostCannotBeWaived() {
        Card vivisection = new Vivisection();
        setupKnowledgePoolManually(List.of(vivisection));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(vivisection.getId()));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(vivisection.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(vivisection);
    }

    @Test
    @DisplayName("A targeted spell with no legal targets remains exiled")
    void spellWithNoLegalTargetsRemainsExiled() {
        Card burn = new BurnTheImpure();
        setupKnowledgePoolManually(List.of(burn));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(burn.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(burn);
    }

    @Test
    @DisplayName("The opponent who casts from hand may cast a card owned by the Pool controller")
    void opponentCanCastAnotherPlayersExiledCard() {
        Card bears = new GrizzlyBears();
        setupKnowledgePoolManually(List.of(bears));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.KnowledgePoolCastChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card instanceof Shock);
        harness.assertLife(player1, 20);
    }

    /**
     * Sets up a Knowledge Pool on player1's battlefield by casting it properly,
     * with known nonland cards in the pool from the ETB.
     */
    private void setupKnowledgePoolWithPool() {
        // Give each player some spells in their library for ETB exile
        harness.setLibrary(player1, List.of(
                new Shock(), new GrizzlyBears(), new Forest(),
                new Forest(), new Forest()
        ));
        harness.setLibrary(player2, List.of(
                new Mountain(), new Mountain(), new Mountain(),
                new Forest(), new Forest()
        ));

        // Cast Knowledge Pool (costs {6})
        harness.setHand(player1, List.of(new KnowledgePool()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → exiles cards to pool
    }

    /**
     * Sets up a Knowledge Pool on player1's battlefield with a manually configured pool.
     * Uses addToBattlefield (no ETB) and manually initializes permanentExiledCards.
     */
    private void setupKnowledgePoolManually(List<Card> poolCards) {
        harness.addToBattlefield(player1, new KnowledgePool());
        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        for (Card card : poolCards) {
            gd.addToExile(player1.getId(), card, kpPermId);
        }
    }
}
