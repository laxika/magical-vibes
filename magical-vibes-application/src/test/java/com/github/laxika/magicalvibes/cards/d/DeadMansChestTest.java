package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.cards.m.MartyrOfDusk;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadMansChest.class, GiantSpider.class, GrizzlyBears.class, Island.class,
        Clone.class, MartyrOfDusk.class, PlanarCleansing.class, Hushbringer.class})
class DeadMansChestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles cards equal to the dying creature's power and permits nonland casts")
    void exilesFromOwnersLibraryAndAllowsControllerToCastNonland() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Card bear = new GrizzlyBears();
        Card island = new Island();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bear, island, remaining));

        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(bear.getId(), island.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions.get(bear.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(bear.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(island.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bear.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Can enchant only a creature an opponent controls")
    void cannotEnchantOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new DeadMansChest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void triggersWhenAuraAndCreatureAreDestroyedSimultaneously() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card first = new GrizzlyBears();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dead Man's Chest");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    void exilesFromOwnersLibraryWhenEnchantedCloneDies() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, spider.getId());
        Permanent clone = findPermanent(player2, "Giant Spider");
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(clone.getId());
        Card first = new GrizzlyBears();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        clone.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Clone");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    void usesLastKnownPowerIncludingCountersAndExilesEntireShortLibrary() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new Island();
        harness.setLibrary(player2, List.of(first, second, third));

        spider.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void zeroPowerCreatureExilesNothing() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.getCounters().put(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card card = new GrizzlyBears();
        harness.setLibrary(player2, List.of(card));

        spider.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
    }

    @Test
    void dyingTokenExilesFromItsOwnersLibrary() {
        Permanent martyr = harness.addToBattlefieldAndReturn(player2, new MartyrOfDusk());
        martyr.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        Permanent vampire = findPermanent(player2, "Vampire");
        harness.setHand(player1, List.of(new DeadMansChest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, vampire.getId());
        harness.passBothPriorities();
        Card card = new GrizzlyBears();
        harness.setLibrary(player2, List.of(card));

        vampire.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    void usesOwnerRatherThanControllerAndPreservesNormalCastingTiming() {
        Card spiderCard = new GiantSpider();
        spiderCard.setOwnerId(player1.getId());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, spiderCard);
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card bear = new GrizzlyBears();
        Card island = new Island();
        harness.setLibrary(player1, List.of(bear, island));
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentCard));

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bear, island);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void hushbringerPreventsEnchantedCreatureDeathFromTriggering() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card first = new GrizzlyBears();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    void castingPermissionSurvivesAuraLeavingAndLaterTurns() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card bear = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bear, new Island(), new Island(), new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Dead Man's Chest");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void gainingControlOfEnchantedCreaturePutsAuraInGraveyardWithoutTriggering() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent chest = harness.addToBattlefieldAndReturn(player1, new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        Card card = new GrizzlyBears();
        harness.setLibrary(player2, List.of(card));

        gd.playerBattlefields.get(player2.getId()).remove(spider);
        gd.playerBattlefields.get(player1.getId()).add(spider);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Dead Man's Chest");
        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
    }
}
