package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.Dehydration;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.c.CurseOfThePiercedHeart;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.o.OrbOfDreams;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSun;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.cards.r.RemoveSoul;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.y.YukoraThePrisoner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarpWorld.class, RodOfRuin.class, Plains.class, GloriousAnthem.class,
        RagingGoblin.class, Pacifism.class, Persuasion.class, Dehydration.class,
        RemoveSoul.class, RootMaze.class, NyxbornCourser.class, OrbOfDreams.class,
        AngelicChorus.class, SoulWarden.class, YukoraThePrisoner.class, OmenOfTheSun.class,
        CurseOfThePiercedHeart.class})
class WarpWorldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts Warp World on stack as a sorcery with no target")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving with only permanents in libraries returns those permanents to battlefield")
    void resolvingWithOnlyPermanentsReturnsPermanentsToBattlefield() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GloriousAnthem());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
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
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent stolenPermanent = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        gd.stolenCreatures.put(stolenPermanent.getId(), player1.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName()).isEqualTo("Raging Goblin");
    }

    @Test
    @DisplayName("Aura enters attached when Warp World reveals a legal target")
    void auraEntersAttachedWithLegalTarget() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent originalAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        originalAura.setAttachedTo(originalCreature.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        Permanent creature = battlefield.stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
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
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent originalAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        originalAura.setAttachedTo(originalCreature.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.pendingAuraCard()).isNotNull();
        assertThat(gd.interaction.pendingAuraCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).hasSize(2);

        UUID chosenTarget = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds().stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, chosenTarget);

        Permanent aura = findPermanent(player1, "Pacifism");
        assertThat(aura.getAttachedTo()).isEqualTo(chosenTarget);
    }

    @Test
    @DisplayName("Control-changing Aura chosen during Warp World steals the enchanted creature")
    void controlAuraChoiceStealsCreature() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent originalAura = harness.addToBattlefieldAndReturn(player1, new Persuasion());
        harness.addToBattlefield(player2, new RagingGoblin());
        originalAura.setAttachedTo(originalCreature.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID opponentCreatureId = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .filter(id -> gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds().contains(id))
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
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent originalAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        harness.addToBattlefield(player1, new GloriousAnthem());
        originalAura.setAttachedTo(originalCreature.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> p.getCard().hasType(CardType.CREATURE));

        UUID chosenTarget = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds().stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, chosenTarget);

        assertThat(countPermanents(player1, "Pacifism")).isEqualTo(1);
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura choices proceed in APNAP order when both players must choose")
    void auraChoicesFollowApnapOrder() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(player2);

        Permanent p1Creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent p1Aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        Permanent p2Creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent p2Aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        p1Aura.setAttachedTo(p1Creature.getId());
        p2Aura.setAttachedTo(p2Creature.getId());

        harness.castFromHand(player2, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());

        UUID p2Choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds().stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player2, p2Choice);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());

        UUID p1Choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds().stream().findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, p1Choice);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Pacifism")).isEqualTo(1);
        assertThat(countPermanents(player2, "Pacifism")).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura with no legal target is not put onto battlefield and goes to bottom")
    void auraWithoutLegalTargetGoesToBottom() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        addAuraAttachedToToken(player1, new Pacifism());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Warp World asks player to choose bottom order when multiple cards remain")
    void warpWorldBottomOrderChoice() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        addAuraAttachedToToken(player1, new Pacifism());
        addAuraAttachedToToken(player1, new Dehydration());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        List<UUID> beforeOrderIds = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards().stream().map(Card::getId).toList();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getId()).isEqualTo(beforeOrderIds.get(1));
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getId()).isEqualTo(beforeOrderIds.get(0));
    }

    @Test
    @DisplayName("Bottom reorder prompts follow APNAP order when both players must reorder")
    void bottomReorderFollowsApnapOrder() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(player2);

        addAuraAttachedToToken(player1, new Pacifism());
        addAuraAttachedToToken(player1, new Dehydration());
        addAuraAttachedToToken(player2, new Pacifism());
        addAuraAttachedToToken(player2, new Dehydration());

        harness.castFromHand(player2, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player2.getId());

        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player1.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Warp World resolves to graveyard after effect")
    void warpWorldGoesToGraveyardAfterResolving() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).hasSize(1);
        assertThat(graveyard.getFirst().getName()).isEqualTo("Warp World");
    }

    @Test
    @DisplayName("Token permanents count toward reveal but are not shuffled into library")
    void tokenPermanentsCountButAreNotShuffled() {
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
        harness.setLibrary(player1, List.of(removeSoul));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        // Token was moved away by Warp World and does not get shuffled into the library.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(removeSoul);
    }

    @Test
    @DisplayName("An artifact entering before Root Maze remains untapped")
    void artifactEnteringBeforeRootMazeRemainsUntapped() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, new RodOfRuin());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent rodOfRuin = findPermanent(player1, "Rod of Ruin");
        assertThat(rodOfRuin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchantment creatures can be enchanted by Auras entering in Warp World's second group")
    void enchantmentCreatureCanBeEnchantedByWarpWorldAura() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent returnedCreature = findPermanent(player1, "Nyxborn Courser");
        Permanent returnedAura = findPermanent(player1, "Pacifism");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Enchantments entering in Warp World's second group see first-group enter-tapped effects")
    void enchantmentGroupSeesFirstGroupEnterTappedEffects() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent anthem = findPermanent(player1, "Glorious Anthem");
        assertThat(anthem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifact enchantments enter the battlefield only once")
    void artifactEnchantmentEntersOnlyOnce() {
        Card artifactEnchantment = new Card();
        artifactEnchantment.setName("Test Artifact Enchantment");
        artifactEnchantment.setType(CardType.ARTIFACT);
        artifactEnchantment.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        artifactEnchantment.setManaCost("");
        artifactEnchantment.setCardText("");

        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, artifactEnchantment);

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Root Maze", "Test Artifact Enchantment");
    }

    @Test
    @DisplayName("A token supplies a reveal even when no permanent cards are shuffled back")
    void tokenSuppliesRevealOfPermanentCard() {
        Card token = new Card();
        token.setName("Goblin Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.RED);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchantments entering after creatures do not trigger for those creatures")
    void laterEnchantmentDoesNotSeeEarlierCreatureEntry() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new AngelicChorus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Creatures entering together see each other's entry triggers")
    void simultaneouslyEnteringCreaturesSeeEachOther() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player2, new RagingGoblin());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Shuffling Yukora away triggers its leaves-the-battlefield ability")
    void shuffledPermanentTriggersLeavesBattlefieldAbility() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new YukoraThePrisoner());
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Yukora, the Prisoner");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Noncreature enchantments retain their own enters-the-battlefield triggers")
    void enteringEnchantmentTriggersItsOwnAbility() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new OmenOfTheSun());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("Auras with enchant player can enter attached to a chosen player")
    void playerAuraCanEnterWithoutAnyPermanentsToEnchant() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        Permanent originalCurse = harness.addToBattlefieldAndReturn(player1, new CurseOfThePiercedHeart());
        originalCurse.setAttachedTo(player2.getId());

        harness.castFromHand(player1, new WarpWorld(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(findPermanent(player1, "Curse of the Pierced Heart").getAttachedTo())
                .isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addAuraAttachedToToken(Player player, Card auraCard) {
        Card tokenCard = new Card();
        tokenCard.setName("Goblin Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setColor(CardColor.RED);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);

        Permanent token = harness.addToBattlefieldAndReturn(player, tokenCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(token.getId());
    }
}

