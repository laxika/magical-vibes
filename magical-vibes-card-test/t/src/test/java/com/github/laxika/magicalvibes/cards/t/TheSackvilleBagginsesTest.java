package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSackvilleBagginses.class, GrizzlyBears.class, MindStone.class})
class TheSackvilleBagginsesTest extends BaseCardTest {

    @Test
    void maySacrificeAnotherCreatureDrawsAndCreatesTreasure() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void maySacrificeAnotherArtifactDrawsAndCreatesTreasure() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void sacrificingATokenMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new TheSackvilleBagginses());
        addTreasureToken(player1);
        int lifeBefore = gd.getLife(player2.getId());

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    void sacrificeRewardHappensDuringTheOriginalAbilityResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningSacrificeDoesNotDrawOrCreateTreasure() {
        harness.addToBattlefield(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Sackville-Bagginses");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sacrificingANontokenArtifactDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player1, new TheSackvilleBagginses());
        harness.addToBattlefield(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void treasureCreatedByEnterAbilityCanBeSacrificedForManaAndLifeLoss() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 20);

        int treasureIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void sacrificingATokenToEnterAbilityDrawsCreatesTreasureAndTriggersLifeLoss() {
        addTreasureToken(player1);
        Permanent treasure = findPermanent(player1, "Treasure");
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, treasure.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(treasure.getId()));
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsTokenSacrificeDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player1, new TheSackvilleBagginses());
        addTreasureToken(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Treasure");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."));
        harness.addToBattlefield(player, treasureCard);
    }
}
