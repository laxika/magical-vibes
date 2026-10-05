package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZoologicalStudy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonsterManualZoologicalStudy.class, ZoologicalStudy.class, GrizzlyBears.class, Forest.class})
class MonsterManualZoologicalStudyTest extends BaseCardTest {

    @Test
    void zoologicalStudyMillsFiveAndReturnsOneMilledCreature() {
        Card creature = new GrizzlyBears();
        Card noncreatureOne = new Forest();
        Card noncreatureTwo = new Forest();
        Card noncreatureThree = new Forest();
        Card noncreatureFour = new Forest();
        MonsterManualZoologicalStudy card = new MonsterManualZoologicalStudy();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, noncreatureOne, noncreatureTwo, noncreatureThree, noncreatureFour));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(indexOf(graveyard, creature));

        harness.handleGraveyardCardChosen(player1, indexOf(graveyard, creature));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(noncreatureOne, noncreatureTwo, noncreatureThree, noncreatureFour);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void monsterManualMayPutCreatureFromHandOntoBattlefield() {
        Permanent manual = harness.addToBattlefieldAndReturn(player1, new MonsterManualZoologicalStudy());
        manual.setSummoningSick(false);
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(manual.isTapped()).isTrue();
    }

    @Test
    void zoologicalStudyDoesNotOfferChoiceWithoutMilledCreature() {
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        MonsterManualZoologicalStudy card = new MonsterManualZoologicalStudy();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, milled);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void monsterManualCanDeclinePuttingCreatureOntoBattlefield() {
        Permanent manual = harness.addToBattlefieldAndReturn(player1, new MonsterManualZoologicalStudy());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(manual);
        assertThat(manual.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zoologicalStudyReturnsOnlyNewlyMilledCreatureFromShortLibrary() {
        Card oldCreature = new GrizzlyBears();
        Card milledCreature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(milledCreature, land));
        MonsterManualZoologicalStudy card = new MonsterManualZoologicalStudy();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(indexOf(graveyard, milledCreature));
        assertThat(choice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, indexOf(graveyard, milledCreature));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milledCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(oldCreature, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void monsterManualCanBeCastFromAdventureExile() {
        MonsterManualZoologicalStudy card = new MonsterManualZoologicalStudy();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Monster Manual");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private int indexOf(List<Card> cards, Card card) {
        return IntStream.range(0, cards.size())
                .filter(index -> cards.get(index).getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
