package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgePoolTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB and cast trigger effects")
    void hasCorrectEffects() {
        KnowledgePool card = new KnowledgePool();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EachPlayerExilesTopCardsToSourceEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(KnowledgePoolCastTriggerEffect.class);
    }

    // ===== ETB — each player exiles top 3 =====

    @Test
    @DisplayName("ETB exiles top 3 cards from each player's library")
    void etbExilesTopThreeFromEachPlayer() {
        // Setup: give each player known cards in their library
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

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
        List<Card> pool = gd.permanentExiledCards.get(kpPermId);
        assertThat(pool).hasSize(6); // 3 from each player

        // Cards should also be in each player's exile zone
        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB exiles fewer cards when library has less than 3")
    void etbExilesFewerWhenLibrarySmall() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Mountain()));

        harness.setHand(player1, List.of(new KnowledgePool()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(1);

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        assertThat(gd.permanentExiledCards.get(kpPermId)).hasSize(3); // 2 + 1
    }

    // ===== Cast trigger — spell from hand =====

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE);
    }

    // ===== Player picks from pool =====

    @Test
    @DisplayName("Player can cast a nonland non-targeted card from the pool without paying mana cost")
    void playerCastsFromPool() {
        // Put a non-targeted creature in the pool
        Card bears = new GrizzlyBears();
        setupKnowledgePoolManually(List.of(bears));
        gd.playerExiledCards.get(player1.getId()).add(bears);

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose the Grizzly Bears from the pool
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // The chosen card should be on the stack
        assertThat(gd.stack).anyMatch(se -> se.getCard().getId().equals(bears.getId()));

        // The chosen card should no longer be in the KP pool
        List<Card> pool = gd.permanentExiledCards.get(kpPermId);
        assertThat(pool).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    // ===== Player declines =====

    @Test
    @DisplayName("Player can decline to cast from the pool")
    void playerDeclinesFromPool() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        int poolSizeBefore = gd.permanentExiledCards.get(kpPermId).size();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Decline by passing empty list
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // Pool should have grown by 1 (the exiled original spell)
        assertThat(gd.permanentExiledCards.get(kpPermId)).hasSize(poolSizeBefore + 1);

        // Interaction should be cleared
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== No re-trigger =====

    @Test
    @DisplayName("Replacement spell cast from KP does NOT re-trigger Knowledge Pool")
    void replacementSpellDoesNotRetrigger() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        List<Card> pool = gd.permanentExiledCards.get(kpPermId);

        Card nonlandCard = pool.stream()
                .filter(c -> c.getType() != CardType.LAND)
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose a card from the pool
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(nonlandCard.getId()));

        // The replacement spell should be on the stack, but NO new KP trigger
        // (KP trigger only fires for cast-from-hand)
        long kpTriggers = gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> se.getEffectsToResolve().stream()
                        .anyMatch(e -> e instanceof KnowledgePoolExileAndCastEffect))
                .count();
        assertThat(kpTriggers).isZero();
    }

    // ===== Original spell gone =====

    @Test
    @DisplayName("If original spell is countered before KP trigger resolves, 'if the player does' fails")
    void originalSpellGoneBeforeTriggerResolves() {
        setupKnowledgePoolWithPool();

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        int poolSizeBefore = gd.permanentExiledCards.get(kpPermId).size();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Manually remove original spell from stack (simulating it being countered)
        gd.stack.removeIf(se -> se.getCard().getName().equals("Counsel of the Soratami"));

        // Now resolve the KP trigger
        harness.passBothPriorities();

        // No choice should be presented (original spell gone)
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Pool should be unchanged
        assertThat(gd.permanentExiledCards.get(kpPermId)).hasSize(poolSizeBefore);
    }

    // ===== Nonland filter =====

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
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== "Other" filter =====

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE);

        var validIds = gd.interaction.knowledgePoolCastChoiceContext().validCardIds();
        assertThat(validIds).contains(shockInPool.getId());

        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        // The just-exiled Counsel should NOT be in the valid choices
        Card exiledCounsel = gd.permanentExiledCards.get(kpPermId).stream()
                .filter(c -> c.getName().equals("Counsel of the Soratami"))
                .findFirst().orElse(null);
        if (exiledCounsel != null) {
            assertThat(validIds).doesNotContain(exiledCounsel.getId());
        }
    }

    // ===== KP destroyed =====

    @Test
    @DisplayName("KP trigger fizzles if Knowledge Pool is destroyed before trigger resolves")
    void kpDestroyedBeforeTriggerResolves() {
        setupKnowledgePoolWithPool();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        // Remove KP from battlefield before resolving trigger
        UUID kpPermId = harness.getPermanentId(player1, "Knowledge Pool");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(kpPermId));

        // Resolve KP trigger
        harness.passBothPriorities();

        // Trigger should fizzle — no choice presented
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Original spell should still be on stack (not exiled)
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Counsel of the Soratami"));
    }

    // ===== Creature from pool =====

    @Test
    @DisplayName("Casting a creature from the KP pool puts it on the stack and enters battlefield")
    void creatureFromPool() {
        Card bears = new GrizzlyBears();
        setupKnowledgePoolManually(List.of(bears));
        gd.playerExiledCards.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose the Grizzly Bears
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // Bears should be on the stack as a creature spell
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getId().equals(bears.getId())
                        && se.getEntryType() == StackEntryType.CREATURE_SPELL);

        // Resolve it
        harness.passBothPriorities();

        // Bears should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    // ===== Targeted spell from pool =====

    @Test
    @DisplayName("Casting a targeted spell from pool prompts for target selection")
    void targetedSpellFromPool() {
        Card shock = new Shock();
        setupKnowledgePoolManually(List.of(shock));
        gd.playerExiledCards.get(player1.getId()).add(shock);

        // Give player2 a creature to target
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve KP trigger

        // Choose Shock from pool
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));

        // Should be waiting for target selection (permanent choice)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose target — Grizzly Bears
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock should now be on the stack targeting bears
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getId().equals(shock.getId())
                        && se.getTargetPermanentId().equals(bearsId));

        // Resolve the Shock
        harness.passBothPriorities();

        // Bears should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Helper methods =====

    /**
     * Sets up a Knowledge Pool on player1's battlefield by casting it properly,
     * with known nonland cards in the pool from the ETB.
     */
    private void setupKnowledgePoolWithPool() {
        // Give each player some spells in their library for ETB exile
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Shock(), new GrizzlyBears(), new Forest(),
                new Forest(), new Forest()
        ));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
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
        gd.permanentExiledCards.put(kpPermId, Collections.synchronizedList(new ArrayList<>(poolCards)));
    }
}
