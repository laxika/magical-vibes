package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TheMendingOfDominaria.class, BalothGorger.class, ShivanFire.class, Forest.class, Plains.class,
        Mountain.class, ValakutTheMoltenPinnacle.class})
class TheMendingOfDominariaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting The Mending of Dominaria adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I mills two cards then offers graveyard creature choice")
    void chapterIMillsThenOffersCreatureChoice() {
        // Put creatures on top of library so they get milled
        BalothGorger bear = new BalothGorger();
        ShivanFire shock = new ShivanFire();
        harness.setLibrary(player1, List.of(bear, shock));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I → mills 2 cards, then graveyard choice

        GameData gd = harness.getGameData();

        // The 2 cards should have been milled to graveyard
        harness.assertInGraveyard(player1, "Baloth Gorger");

        // Should be awaiting graveyard choice for the creature
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Chapter I with no creatures milled does not offer graveyard choice")
    void chapterINoCreaturesMilledSkipsChoice() {
        // Put only non-creature cards on top of library
        ShivanFire shock1 = new ShivanFire();
        ShivanFire shock2 = new ShivanFire();
        harness.setLibrary(player1, List.of(shock1, shock2));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        // No graveyard choice since no creatures were milled (and none were in graveyard)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter I can return a creature that was already in graveyard before milling")
    void chapterICanReturnCreatureAlreadyInGraveyard() {
        // Put a creature in graveyard and non-creatures on top of library
        BalothGorger bear = new BalothGorger();
        harness.setGraveyard(player1, List.of(bear));
        harness.setLibrary(player1, List.of(new ShivanFire(), new ShivanFire()));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I → mills 2 non-creatures, then graveyard choice

        GameData gd = harness.getGameData();

        // Should still offer graveyard choice for the creature that was already in graveyard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Chapter III returns all land cards from graveyard to battlefield")
    void chapterIIIReturnsLandsToBattlefield() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Put lands in graveyard
        Forest forest1 = new Forest();
        Forest forest2 = new Forest();
        Plains plains = new Plains();
        BalothGorger bear = new BalothGorger(); // non-land should not be returned
        harness.setGraveyard(player1, List.of(forest1, forest2, plains, bear));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // All 3 lands should be on the battlefield
        long landCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(landCount).isEqualTo(3);

        // Non-land creature should NOT be on the battlefield
        boolean bearOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Baloth Gorger"));
        assertThat(bearOnBf).isFalse();
    }

    @Test
    @DisplayName("Chapter III shuffles remaining graveyard into library after returning lands")
    void chapterIIIShufflesGraveyardIntoLibrary() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Put lands and a creature in graveyard
        Forest forest = new Forest();
        BalothGorger bear = new BalothGorger();
        ShivanFire shock = new ShivanFire();
        harness.setGraveyard(player1, List.of(forest, bear, shock));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // Forest should be on battlefield (returned)
        boolean forestOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(forestOnBf).isTrue();

        // Graveyard should only contain the saga itself (sacrificed after chapter III resolves)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("The Mending of Dominaria");

        // Library should have gained the non-land cards (bear + shock were shuffled in)
        assertThat(gd.playerDecks.get(player1.getId()).size()).isGreaterThan(deckSizeBefore);
    }

    @Test
    @DisplayName("Chapter III with no lands in graveyard still shuffles graveyard into library")
    void chapterIIINoLandsStillShuffles() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Only non-lands in graveyard
        BalothGorger bear = new BalothGorger();
        harness.setGraveyard(player1, List.of(bear));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // No lands on battlefield (except any that were already there)
        boolean bearOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Baloth Gorger"));
        assertThat(bearOnBf).isFalse();

        // Graveyard should only contain the saga itself (sacrificed after chapter III resolves)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("The Mending of Dominaria");

        // Library should have gained the bear (shuffled in)
        assertThat(gd.playerDecks.get(player1.getId()).size()).isGreaterThan(deckSizeBefore);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed (no longer on battlefield)
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("The Mending of Dominaria"));
        assertThat(sagaStillOnBf).isFalse();
    }

    @Test
    @DisplayName("Chapter I can return a newly milled creature to hand")
    void chapterIReturnsMilledCreature() {
        BalothGorger creature = new BalothGorger();
        ShivanFire instant = new ShivanFire();
        harness.setLibrary(player1, List.of(creature, instant));
        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter I can decline returning a creature without undoing the mill")
    void chapterICanDeclineReturn() {
        BalothGorger creature = new BalothGorger();
        ShivanFire instant = new ShivanFire();
        harness.setLibrary(player1, List.of(creature, instant));
        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter II mills the remaining card of a short library and returns an existing creature")
    void chapterIIReturnsExistingCreatureWithShortLibrary() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        saga.setCounterCount(CounterType.LORE, 1);
        BalothGorger creature = new BalothGorger();
        ShivanFire instant = new ShivanFire();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(instant));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(findPermanent(player1, "The Mending of Dominaria")).isSameAs(saga);
    }

    @Test
    @DisplayName("Chapter III returns lands simultaneously so both Mountains trigger Valakut")
    void chapterIIIReturnsMountainsSimultaneously() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = findPermanent(player1, "The Mending of Dominaria");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 14);
    }
}
