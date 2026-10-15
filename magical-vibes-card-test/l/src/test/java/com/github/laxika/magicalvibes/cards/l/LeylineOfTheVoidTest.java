package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.b.BackToNature;
import com.github.laxika.magicalvibes.cards.m.MitoticSlime;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.ShriekingGrotesque;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
@ExtendWith(CardUsedExtension.class)
@CardUsed({LeylineOfTheVoid.class, DaggerclawImp.class, Mortify.class, ShriekingGrotesque.class,
        BackToNature.class, MitoticSlime.class, TomeScour.class})
class LeylineOfTheVoidTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gd.alwaysOfferPriorityWindows = true;
        // Do NOT call skipMulligan() here — leyline tests need to set hand first
    }

    // ===== Leyline opening hand mechanic =====

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfTheVoid()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfTheVoid()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Leyline of the Void");
        harness.assertNotInHand(player1, "Leyline of the Void");
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfTheVoid()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Leyline of the Void");
        harness.assertInHand(player1, "Leyline of the Void");
    }

    // ===== Exile replacement — creature dying =====

    @Test
    @DisplayName("Opponent creature destroyed by a spell is exiled instead of going to graveyard")
    void opponentCreatureExiledInsteadOfGraveyard() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addToBattlefield(player2, new DaggerclawImp());

        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Daggerclaw Imp");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Creature should not be on the battlefield
        harness.assertNotOnBattlefield(player2, "Daggerclaw Imp");
        // Creature should be exiled, not in graveyard
        harness.assertNotInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Daggerclaw Imp"));
    }

    @Test
    @DisplayName("Leyline does not exile an opponent-controlled permanent owned by its controller")
    void leylineChecksGraveyardOwnerInsteadOfBattlefieldController() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        DaggerclawImp targetCard = new DaggerclawImp();
        targetCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, targetCard);

        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Daggerclaw Imp");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c == targetCard);
    }

    @Test
    @DisplayName("Leyline exiles an opponent-owned permanent controlled by Leyline's controller")
    void leylineExilesOpponentOwnedPermanentDespiteControlChange() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        DaggerclawImp targetCard = new DaggerclawImp();
        targetCard.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, targetCard);

        harness.setHand(player2, List.of(new Mortify()));
        addMortifyMana(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID targetId = harness.getPermanentId(player1, "Daggerclaw Imp");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertNotInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(targetCard);
    }

    @Test
    @DisplayName("Leyline does not exile an opponent's token")
    void leylineDoesNotExileOpponentToken() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        DaggerclawImp tokenCard = new DaggerclawImp();
        tokenCard.setToken(true);
        harness.addToBattlefield(player2, tokenCard);

        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Daggerclaw Imp");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c == tokenCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c == tokenCard);
    }

    // ===== Exile replacement — via opening hand placement =====

    @Test
    @DisplayName("Leyline placed from opening hand exiles opponent creatures that die")
    void leylineFromOpeningHandExilesOpponentCreatures() {
        harness.setHand(player1, List.of(new LeylineOfTheVoid()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);

        harness.addToBattlefield(player2, new DaggerclawImp());

        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Daggerclaw Imp");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Creature should be exiled, not in graveyard
        harness.assertNotInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Daggerclaw Imp"));
    }

    // ===== Exile replacement — spell goes to graveyard after resolution =====

    @Test
    @DisplayName("Opponent's spell is exiled after resolution instead of going to graveyard")
    void opponentSpellExiledAfterResolution() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addToBattlefield(player1, new DaggerclawImp());

        harness.setHand(player2, List.of(new Mortify()));
        addMortifyMana(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID targetId = harness.getPermanentId(player1, "Daggerclaw Imp");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertNotInGraveyard(player2, "Mortify");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mortify"));
    }

    @Test
    @DisplayName("Opponent cards discarded from hand are exiled instead of going to the graveyard")
    void opponentDiscardedCardIsExiledInsteadOfGraveyard() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        DaggerclawImp discardedCard = new DaggerclawImp();
        harness.setHand(player2, List.of(new ShriekingGrotesque(), discardedCard));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discardedCard);
    }

    // ===== Only affects opponents =====

    @Test
    @DisplayName("Controller's own cards go to graveyard normally")
    void controllerOwnCardsGoToGraveyardNormally() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addToBattlefield(player1, new DaggerclawImp());

        harness.setHand(player2, List.of(new Mortify()));
        addMortifyMana(player2);

        UUID targetId = harness.getPermanentId(player1, "Daggerclaw Imp");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, targetId);

        // Controller's creature should go to graveyard, NOT exile
        harness.assertInGraveyard(player1, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Daggerclaw Imp"));
    }

    // ===== Effect goes away when Leyline leaves =====

    @Test
    @DisplayName("Opponent creatures go to graveyard normally after Leyline leaves the battlefield")
    void effectStopsWhenLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.addToBattlefield(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Daggerclaw Imp");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Without Leyline, creature should go to graveyard normally
        harness.assertInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Daggerclaw Imp"));
    }

    // ===== Can be cast normally =====

    @Test
    @DisplayName("Leyline of the Void can be cast normally for {2}{B}{B}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfTheVoid()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leyline of the Void");
    }

    private void addMortifyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Multiple opening-hand Leylines can each begin on the battlefield")
    void multipleOpeningHandLeylines() {
        harness.setHand(player1, List.of(new LeylineOfTheVoid(), new LeylineOfTheVoid()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Leyline of the Void"))
                .hasSize(2);
        harness.assertNotInHand(player1, "Leyline of the Void");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milling exiles opponent cards without exiling existing graveyard cards")
    void millingOpponentCardsExilesThem() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        LeylineOfTheVoid existingCard = new LeylineOfTheVoid();
        harness.setGraveyard(player2, List.of(existingCard));
        List<LeylineOfTheVoid> milledCards = List.of(new LeylineOfTheVoid(), new LeylineOfTheVoid(),
                new LeylineOfTheVoid(), new LeylineOfTheVoid(), new LeylineOfTheVoid());
        harness.setLibrary(player2, milledCards);
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(existingCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsAll(milledCards);
        harness.assertInGraveyard(player1, "Tome Scour");
    }

    @Test
    @DisplayName("Both opposing Leylines apply when all enchantments are destroyed simultaneously")
    void simultaneousDestructionPreservesBothReplacementEffects() {
        harness.skipMulligan();
        LeylineOfTheVoid firstLeyline = new LeylineOfTheVoid();
        LeylineOfTheVoid secondLeyline = new LeylineOfTheVoid();
        harness.addToBattlefield(player1, firstLeyline);
        harness.addToBattlefield(player2, secondLeyline);
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Leyline of the Void");
        harness.assertNotOnBattlefield(player2, "Leyline of the Void");
        harness.assertNotInGraveyard(player1, "Leyline of the Void");
        harness.assertNotInGraveyard(player2, "Leyline of the Void");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(firstLeyline);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(secondLeyline);
        harness.assertInGraveyard(player1, "Back to Nature");
    }

    @Test
    @DisplayName("An opponent's real Ooze token still dies and creates smaller Oozes")
    void opponentTokenDeathAbilityStillTriggers() {
        harness.skipMulligan();
        harness.addToBattlefield(player2, new MitoticSlime());
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Mitotic Slime"));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        UUID tokenId = harness.getPermanentId(player2, "Ooze");
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana(player1);
        harness.castAndResolveInstant(player1, 0, tokenId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(p -> p.getCard().getName().equals("Ooze"));
    }
}
