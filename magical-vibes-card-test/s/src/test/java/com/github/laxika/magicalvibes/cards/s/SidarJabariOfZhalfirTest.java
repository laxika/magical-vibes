package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheWhiteOrchid;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidarJabariOfZhalfir.class, BurnishedHart.class, KnightOfTheWhiteOrchid.class, MindStone.class})
class SidarJabariOfZhalfirTest extends BaseCardTest {

    @Test
    void commandZoneEminenceCreatesExactlyOneTriggerForMultipleKnights() {
        gd.playerCommandZones.get(player1.getId()).add(new SidarJabariOfZhalfir());
        addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone(), new MindStone()));

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void commandZoneEminenceStillLootsAfterTheAttackingKnightLeaves() {
        gd.playerCommandZones.get(player1.getId()).add(new SidarJabariOfZhalfir());
        Permanent knight = addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        gd.playerGraveyards.get(player1.getId()).add(knight.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Burnished Hart");
    }

    @Test
    void commandZoneEminenceDoesNotTriggerForANonKnight() {
        gd.playerCommandZones.get(player1.getId()).add(new SidarJabariOfZhalfir());
        addCreatureReady(player1, new BurnishedHart());
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void battlefieldEminenceDoesNothingAfterSidarMovesToTheCommandZone() {
        Permanent sidar = addCreatureReady(player1, new SidarJabariOfZhalfir());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(sidar);
        gd.playerCommandZones.get(player1.getId()).add(sidar.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Burnished Hart");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void combatDamageCannotReturnAKnightFromAnOpponentsGraveyard() {
        Card knight = new KnightOfTheWhiteOrchid();
        harness.setGraveyard(player1, List.of(new BurnishedHart()));
        harness.setGraveyard(player2, List.of(knight));
        Permanent sidar = addCreatureReady(player1, new SidarJabariOfZhalfir());
        sidar.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(knight);
        harness.assertNotOnBattlefield(player1, "Knight of the White Orchid");
    }

    @Test
    void battlefieldEminenceStillLootsAfterTheAttackingKnightLeaves() {
        addCreatureReady(player1, new SidarJabariOfZhalfir());
        Permanent knight = addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        gd.playerGraveyards.get(player1.getId()).add(knight.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Burnished Hart");
    }

    @Test
    void combatDamageDoesNotChooseAnotherKnightWhenItsTargetLeavesTheGraveyard() {
        Card target = new KnightOfTheWhiteOrchid();
        Card otherKnight = new KnightOfTheWhiteOrchid();
        harness.setGraveyard(player1, List.of(target, otherKnight));
        Permanent sidar = addCreatureReady(player1, new SidarJabariOfZhalfir());
        sidar.setAttacking(true);
        resolveCombat();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Knight of the White Orchid");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherKnight);
        harness.assertInHand(player1, "Knight of the White Orchid");
    }

    @Test
    void eminenceLootsWhenAKnightAttacksFromTheBattlefield() {
        addCreatureReady(player1, new SidarJabariOfZhalfir());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Burnished Hart");
    }

    @Test
    void eminenceWorksFromTheCommandZone() {
        Card sidar = new SidarJabariOfZhalfir();
        gd.playerCommandZones.get(player1.getId()).add(sidar);
        addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.setLibrary(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Burnished Hart");
    }

    @Test
    void combatDamageReturnsOnlyATargetedKnightFromTheGraveyard() {
        Card knight = new KnightOfTheWhiteOrchid();
        Card nonKnight = new BurnishedHart();
        harness.setGraveyard(player1, List.of(nonKnight, knight));

        Permanent sidar = addCreatureReady(player1, new SidarJabariOfZhalfir());
        sidar.setAttacking(true);
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(knight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(knight.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Knight of the White Orchid");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonKnight);
    }
}
