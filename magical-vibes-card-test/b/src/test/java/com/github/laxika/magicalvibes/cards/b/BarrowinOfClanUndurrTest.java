package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ClatteringSkeletons;
import com.github.laxika.magicalvibes.cards.d.DwarfholdChampion;
import com.github.laxika.magicalvibes.cards.p.PortableHole;
import com.github.laxika.magicalvibes.cards.p.PriestOfAncientLore;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowinOfClanUndurr.class, DwarfholdChampion.class, ClatteringSkeletons.class, PortableHole.class, PriestOfAncientLore.class})
class BarrowinOfClanUndurrTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield lets its controller choose a dungeon")
    void entersDungeon() {
        harness.castFromHand(player1, new BarrowinOfClanUndurr(), "{2}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Does not return a creature before its controller completes a dungeon")
    void doesNotReturnBeforeDungeonCompletion() {
        DwarfholdChampion champion = new DwarfholdChampion();
        harness.setGraveyard(player1, List.of(champion));
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(champion.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(champion.getId()));
    }

    @Test
    @DisplayName("Checks dungeon completion when the attack ability resolves")
    void checksDungeonCompletionOnResolution() {
        DwarfholdChampion champion = new DwarfholdChampion();
        harness.setGraveyard(player1, List.of(champion));
        addAttackingBarrowin();

        declareAttackers(List.of(0));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(champion.getId()));
    }

    @Test
    @DisplayName("Returns an eligible creature after its controller completes a dungeon")
    void returnsEligibleCreatureAfterDungeonCompletion() {
        DwarfholdChampion champion = new DwarfholdChampion();
        harness.setGraveyard(player1, List.of(champion));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(champion.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(champion.getId()));
    }

    @Test
    @DisplayName("Returns at most one creature and excludes creatures with mana value greater than three")
    void returnsAtMostOneEligibleCreature() {
        DwarfholdChampion firstChampion = new DwarfholdChampion();
        DwarfholdChampion secondChampion = new DwarfholdChampion();
        ClatteringSkeletons skeletons = new ClatteringSkeletons();
        harness.setGraveyard(player1, List.of(firstChampion, secondChampion, skeletons));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(firstChampion.getId())
                        || permanent.getCard().getId().equals(secondChampion.getId())))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(skeletons.getId()));
    }

    @Test
    @DisplayName("Can decline to return the only eligible creature")
    void canDeclineOnlyEligibleCreature() {
        DwarfholdChampion champion = new DwarfholdChampion();
        harness.setGraveyard(player1, List.of(champion));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);
        harness.assertNotOnBattlefield(player1, "Dwarfhold Champion");
        harness.assertInGraveyard(player1, "Dwarfhold Champion");
    }

    @Test
    @DisplayName("An opponent's completed dungeon does not enable the attack ability")
    void opponentsCompletedDungeonDoesNotCount() {
        harness.setGraveyard(player1, List.of(new DwarfholdChampion()));
        gd.playersWhoCompletedDungeon.add(player2.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        harness.assertNotOnBattlefield(player1, "Dwarfhold Champion");
        harness.assertInGraveyard(player1, "Dwarfhold Champion");
    }

    @Test
    @DisplayName("Venturing in an existing dungeon lets the controller choose the next room")
    void choosesNextRoom() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.castFromHand(player1, new BarrowinOfClanUndurr(), "{2}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Returns a mana value three creature but not a noncreature or an opponent's card")
    void returnsManaValueThreeCreatureFromOwnGraveyard() {
        PriestOfAncientLore priest = new PriestOfAncientLore();
        harness.setGraveyard(player1, List.of(priest, new PortableHole()));
        harness.setGraveyard(player2, List.of(new DwarfholdChampion()));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Priest of Ancient Lore");
        harness.assertNotInGraveyard(player1, "Priest of Ancient Lore");
        harness.assertInGraveyard(player1, "Portable Hole");
        harness.assertInGraveyard(player2, "Dwarfhold Champion");
        harness.assertNotOnBattlefield(player1, "Dwarfhold Champion");
    }

    private void addAttackingBarrowin() {
        Permanent barrowin = harness.addToBattlefieldAndReturn(player1, new BarrowinOfClanUndurr());
        barrowin.setSummoningSick(false);
    }

    private void resolveAttackTrigger() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
