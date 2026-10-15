package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VeteranDungeoneer;
import com.github.laxika.magicalvibes.cards.y.YouFindTheVillainsLair;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SefrisOfTheHiddenWays.class, VeteranDungeoneer.class, YouFindTheVillainsLair.class})
class SefrisOfTheHiddenWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Venture triggers once each turn when a creature card enters your graveyard")
    void venturesOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new VeteranDungeoneer());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new VeteranDungeoneer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstCreature));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondCreature));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Completing a dungeon returns a target creature card from your graveyard")
    void returnsCreatureOnDungeonCompletion() {
        VeteranDungeoneer target = new VeteranDungeoneer();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));
        harness.setLibrary(player1, List.of(new VeteranDungeoneer()));

        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void doesNotVentureWhenSefrisItselfDies() {
        Permanent sefris = harness.addToBattlefieldAndReturn(player1, new SefrisOfTheHiddenWays());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sefris));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertInGraveyard(player1, "Sefris of the Hidden Ways");
    }

    @Test
    void opponentsCreatureDoesNotConsumeTheTurnTrigger() {
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new VeteranDungeoneer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VeteranDungeoneer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opposingCreature));
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ownCreature));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void discardingTwoCreaturesVenturesOnlyOnce() {
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 1));
        harness.setLibrary(player1, List.of(new VeteranDungeoneer(), new VeteranDungeoneer()));
        harness.setHand(player1, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Bazaar");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 2));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counteredCreatureSpellTriggersVenture() {
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        VeteranDungeoneer creature = new VeteranDungeoneer();
        harness.setHand(player2, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, creature, "{3}{W}");
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, creature.getId(), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        harness.assertInGraveyard(player1, "Veteran Dungeoneer");
        harness.assertNotOnBattlefield(player1, "Veteran Dungeoneer");
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    void opponentsDungeonCompletionDoesNotReturnYourCreature() {
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        harness.setGraveyard(player1, List.of(new VeteranDungeoneer()));
        harness.setLibrary(player2, List.of(new VeteranDungeoneer()));
        gd.playerDungeonProgress.put(player2.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));

        harness.enterBattlefieldAndReturn(player2, new VeteranDungeoneer());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Veteran Dungeoneer");
        harness.assertNotOnBattlefield(player1, "Veteran Dungeoneer");
    }

    @Test
    void completionTargetsOnlyCreatureCardsInControllersGraveyard() {
        VeteranDungeoneer ownCreature = new VeteranDungeoneer();
        VeteranDungeoneer opposingCreature = new VeteranDungeoneer();
        YouFindTheVillainsLair instant = new YouFindTheVillainsLair();
        harness.setGraveyard(player1, List.of(ownCreature, instant));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setLibrary(player1, List.of(new VeteranDungeoneer()));
        harness.addToBattlefield(player1, new SefrisOfTheHiddenWays());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));

        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 6));
        harness.assertInGraveyard(player1, "Veteran Dungeoneer");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownCreature.getId()));
        harness.assertInGraveyard(player1, "You Find the Villains' Lair");
        harness.assertInGraveyard(player2, "Veteran Dungeoneer");
    }
}
