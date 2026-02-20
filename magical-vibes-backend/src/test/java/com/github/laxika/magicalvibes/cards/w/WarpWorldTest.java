package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Dehydration;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RemoveSoul;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarpWorldTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Warp World has correct card properties")
    void hasCorrectProperties() {
        WarpWorld card = new WarpWorld();

        assertThat(card.getName()).isEqualTo("Warp World");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{5}{R}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(WarpWorldEffect.class);
    }

    @Test
    @DisplayName("Casting puts Warp World on stack as a sorcery with no target")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Warp World");
        assertThat(entry.getTargetPermanentId()).isNull();
    }

    @Test
    @DisplayName("Resolving with only permanents in libraries returns those permanents to battlefield")
    void resolvingWithOnlyPermanentsReturnsPermanentsToBattlefield() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GloriousAnthem());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        Set<String> player1Names = gd.playerBattlefields.get(player1.getId()).stream()
                .map(p -> p.getCard().getName())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        assertThat(player1Names).containsExactlyInAnyOrder("Rod of Ruin", "Plains");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCard().getName())
                .isEqualTo("Glorious Anthem");
    }

    @Test
    @DisplayName("Warp World shuffles each permanent into its owner's library, not controller's")
    void shufflesToOwnerLibraryNotControllerLibrary() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent stolenPermanent = gd.playerBattlefields.get(player2.getId()).getFirst();
        gd.stolenCreatures.put(stolenPermanent.getId(), player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName()).isEqualTo("Raging Goblin");
    }

    @Test
    @DisplayName("Aura enters attached when Warp World reveals a legal target")
    void auraEntersAttachedWithLegalTarget() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Pacifism());
        List<Permanent> startBattlefield = gd.playerBattlefields.get(player1.getId());
        startBattlefield.get(1).setAttachedTo(startBattlefield.get(0).getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        Permanent creature = battlefield.stream()
                .filter(p -> p.getCard().getType() == CardType.CREATURE)
                .findFirst()
                .orElseThrow();
        Permanent aura = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst()
                .orElseThrow();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Warp World prompts Aura controller to choose attachment among legal permanents")
    void warpWorldPromptsAuraAttachmentChoice() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Pacifism());
        List<Permanent> startBattlefield = gd.playerBattlefields.get(player1.getId());
        startBattlefield.get(2).setAttachedTo(startBattlefield.get(0).getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.pendingAuraCard).isNotNull();
        assertThat(gd.interaction.pendingAuraCard.getName()).isEqualTo("Pacifism");
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).hasSize(2);

        UUID chosenTarget = gd.interaction.awaitingPermanentChoiceValidIds.stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, chosenTarget);

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(chosenTarget);
    }

    @Test
    @DisplayName("Control-changing Aura chosen during Warp World steals the enchanted creature")
    void controlAuraChoiceStealsCreature() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Persuasion());
        harness.addToBattlefield(player2, new RagingGoblin());
        List<Permanent> p1Start = gd.playerBattlefields.get(player1.getId());
        p1Start.get(1).setAttachedTo(p1Start.get(0).getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        UUID opponentCreatureId = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .filter(id -> gd.interaction.awaitingPermanentChoiceValidIds.contains(id))
                .findFirst()
                .orElseThrow();

        harness.handlePermanentChosen(player1, opponentCreatureId);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().anyMatch(p -> p.getId().equals(opponentCreatureId))).isTrue();
    }

    @Test
    @DisplayName("Non-Aura enchantments wait to enter until Warp World Aura choices are finished")
    void enchantmentsDeferredUntilAuraChoicesComplete() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player1, new GloriousAnthem());
        List<Permanent> startBattlefield = gd.playerBattlefields.get(player1.getId());
        startBattlefield.get(2).setAttachedTo(startBattlefield.get(0).getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> p.getCard().getType() == CardType.CREATURE);

        UUID chosenTarget = gd.interaction.awaitingPermanentChoiceValidIds.stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, chosenTarget);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().getName().equals("Pacifism")).count()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().getName().equals("Glorious Anthem")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura choices proceed in APNAP order when both players must choose")
    void auraChoicesFollowApnapOrder() {
        harness.setHand(player2, List.of(new WarpWorld()));
        harness.addMana(player2, ManaColor.RED, 8);
        harness.forceActivePlayer(player2);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new Pacifism());

        gd.playerBattlefields.get(player1.getId()).get(2).setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(0).getId());
        gd.playerBattlefields.get(player2.getId()).get(2).setAttachedTo(gd.playerBattlefields.get(player2.getId()).get(0).getId());

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId).isEqualTo(player2.getId());

        UUID p2Choice = gd.interaction.awaitingPermanentChoiceValidIds.stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player2, p2Choice);

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());

        UUID p1Choice = gd.interaction.awaitingPermanentChoiceValidIds.stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, p1Choice);

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().getName().equals("Pacifism")).count()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream().filter(p -> p.getCard().getName().equals("Pacifism")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura with no legal target is not put onto battlefield and goes to bottom")
    void auraWithoutLegalTargetGoesToBottom() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new Pacifism());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Warp World asks player to choose bottom order when multiple cards remain")
    void warpWorldBottomOrderChoice() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player1, new Dehydration());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibraryReorderToBottom).isTrue();
        assertThat(gd.interaction.awaitingLibraryReorderCards).hasSize(2);

        List<UUID> beforeOrderIds = gd.interaction.awaitingLibraryReorderCards.stream().map(Card::getId).toList();
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getId()).isEqualTo(beforeOrderIds.get(1));
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getId()).isEqualTo(beforeOrderIds.get(0));
    }

    @Test
    @DisplayName("Bottom reorder prompts follow APNAP order when both players must reorder")
    void bottomReorderFollowsApnapOrder() {
        harness.setHand(player2, List.of(new WarpWorld()));
        harness.addMana(player2, ManaColor.RED, 8);
        harness.forceActivePlayer(player2);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player1, new Dehydration());
        harness.addToBattlefield(player2, new Pacifism());
        harness.addToBattlefield(player2, new Dehydration());

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderToBottom).isTrue();
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId).isEqualTo(player2.getId());

        harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(1, 0));

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderToBottom).isTrue();
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId).isEqualTo(player1.getId());

        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Warp World resolves to graveyard after effect")
    void warpWorldGoesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerHands.put(player1.getId(), new ArrayList<>(List.of(new WarpWorld())));
        gd.playerHands.put(player2.getId(), new ArrayList<>(List.of()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).hasSize(1);
        assertThat(graveyard.getFirst().getName()).isEqualTo("Warp World");
    }

    @Test
    @DisplayName("Token permanents count toward reveal but are not shuffled into library")
    void tokenPermanentsCountButAreNotShuffled() {
        harness.setHand(player1, List.of(new WarpWorld()));
        harness.addMana(player1, ManaColor.RED, 8);

        Card token = new Card();
        token.setName("Goblin Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.RED);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        RemoveSoul removeSoul = new RemoveSoul();
        Plains plains = new Plains();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(removeSoul, plains)));
        gd.playerDecks.put(player2.getId(), new ArrayList<>());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Token was moved away by Warp World and does not get shuffled into the library.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains", "Remove Soul");
    }
}

