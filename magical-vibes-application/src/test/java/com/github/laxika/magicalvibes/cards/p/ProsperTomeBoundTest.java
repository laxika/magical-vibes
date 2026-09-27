package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperTomeBound.class, Forest.class, GrizzlyBears.class})
class ProsperTomeBoundTest extends BaseCardTest {

    @Test
    void exilesTheTopCardAtTheEndStepAndGrantsPlayPermission() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ProsperTomeBound()));
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void createsATreasureWhenYouPlayALandFromExile() {
        addCreatureReady(player1, new ProsperTomeBound());
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    void createsATreasureWhenYouCastASpellFromExile() {
        addCreatureReady(player1, new ProsperTomeBound());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player1, spell.getId(), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }
}
