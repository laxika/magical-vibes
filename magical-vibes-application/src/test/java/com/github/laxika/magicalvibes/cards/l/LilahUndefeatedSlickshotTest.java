package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilahUndefeatedSlickshot.class, Counterspell.class, GrizzlyBears.class, Shock.class, Terminate.class})
class LilahUndefeatedSlickshotTest extends BaseCardTest {

    private Permanent addLilah() {
        harness.addToBattlefield(player1, new LilahUndefeatedSlickshot());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Prowess boosts Lilah when a noncreature spell is cast")
    void prowessBoostsForNoncreatureSpell() {
        Permanent lilah = addLilah();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lilah)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lilah)).isEqualTo(4);
    }

    @Test
    @DisplayName("A multicolored instant cast from hand becomes plotted as it resolves")
    void plotsMulticoloredInstantFromHand() {
        addLilah();
        GrizzlyBears bears = new GrizzlyBears();
        Terminate terminate = new Terminate();
        harness.addToBattlefield(player2, bears);
        harness.setHand(player1, List.of(terminate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(terminate);
        assertThat(gd.plottedCardIds).contains(terminate.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(terminate.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(terminate.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(terminate);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A monocolored instant is not plotted")
    void doesNotPlotMonocoloredInstant() {
        addLilah();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.plottedCardIds).doesNotContain(shock.getId());
    }

    @Test
    @DisplayName("A countered spell is not plotted")
    void doesNotPlotCounteredSpell() {
        addLilah();
        Terminate terminate = new Terminate();
        harness.setHand(player1, List.of(terminate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, terminate.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(terminate);
        assertThat(gd.plottedCardIds).doesNotContain(terminate.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(terminate);
    }
}
