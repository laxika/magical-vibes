package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowinOfClanUndurr.class, GrizzlyBears.class, HillGiant.class})
class BarrowinOfClanUndurrTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        harness.setHand(player1, List.of(new BarrowinOfClanUndurr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Does not return a creature before its controller completes a dungeon")
    void doesNotReturnBeforeDungeonCompletion() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Checks dungeon completion when the attack ability resolves")
    void checksDungeonCompletionOnResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addAttackingBarrowin();

        declareAttackers(List.of(0));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Returns an eligible creature after its controller completes a dungeon")
    void returnsEligibleCreatureAfterDungeonCompletion() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Returns at most one creature and excludes creatures with mana value greater than three")
    void returnsAtMostOneEligibleCreature() {
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(firstBears, secondBears, giant));
        gd.playersWhoCompletedDungeon.add(player1.getId());
        addAttackingBarrowin();

        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(firstBears.getId())
                        || permanent.getCard().getId().equals(secondBears.getId())))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giant.getId()));
    }

    private Permanent addAttackingBarrowin() {
        Permanent barrowin = harness.addToBattlefieldAndReturn(player1, new BarrowinOfClanUndurr());
        barrowin.setSummoningSick(false);
        return barrowin;
    }

    private void resolveAttackTrigger() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
