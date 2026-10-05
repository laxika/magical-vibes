package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.d.DwalinWeaponmaster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KLiTheResourceful.class, BronzeSword.class, DwalinWeaponmaster.class,
        Forest.class, FountainOfYouth.class, GrizzlyBears.class, TurnToFrog.class,
        Panharmonicon.class})
class KLiTheResourcefulTest extends BaseCardTest {

    @Test
    void drawsOnlyOnceWhenAnotherDwarfAndEquipmentEnter() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());

        harness.enterBattlefieldAndReturn(player1, new DwalinWeaponmaster());
        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenKiliEnters() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new KLiTheResourceful());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void firstEquipEachTurnIsFreeAfterEnduringStory() {
        addStoriedKili();
        Permanent firstSword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent secondSword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(firstSword), null, creature.getId());
        harness.passBothPriorities();

        assertThat(firstSword.getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(secondSword), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void equipIsNotFreeBeforeEnduringStory() {
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(sword), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void drawsForAnotherDwarfWithoutEquipmentEntering() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());

        harness.enterBattlefieldAndReturn(player1, new DwalinWeaponmaster());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void equipmentTriggerConsumesTheDwarfTriggerForTheTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());

        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new DwalinWeaponmaster());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void unrelatedAndOpposingEntriesDoNotConsumeTheTrigger() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player2, new DwalinWeaponmaster());
        harness.enterBattlefieldAndReturn(player2, new BronzeSword());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawLimitResetsOnTheOpponentsTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void equipmentDoesNotTriggerAfterKiliLosesAllAbilities() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent kili = harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, kili.getId());

        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void anEquipActivatedBeforeEnduringStoryStillConsumesTheFirstEquip() {
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sword), null, creature.getId());
        resolveAllTriggers();
        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(sword), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void firstEquipIsFreeAgainOnTheNextTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addStoriedKili();
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(sword), null, firstCreature.getId());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(sword), null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(sword.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    private void addStoriedKili() {
        harness.enterBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
    }

    @Test
    void panharmoniconCannotDoubleTheEquipmentDrawTrigger() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.addToBattlefieldAndReturn(player1, new Panharmonicon());

        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void panharmoniconCannotDoubleTheDwarfDrawTrigger() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.addToBattlefieldAndReturn(player1, new Panharmonicon());

        harness.enterBattlefieldAndReturn(player1, new DwalinWeaponmaster());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
