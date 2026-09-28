package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KefkaDancingMad.class, Opt.class})
class KefkaDancingMadTest extends BaseCardTest {

    @Test
    @DisplayName("Kefka has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent kefka = addCreatureReady(player1, new KefkaDancingMad());
        kefka.setMarkedDamage(6);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kefka);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kefka);
    }

    @Test
    @DisplayName("Kefka exiles a random opposing graveyard card and makes its owner lose mana value life")
    void exilesAndCastsOpposingGraveyardCard() {
        addCreatureReady(player1, new KefkaDancingMad());
        Opt opt = new Opt();
        opt.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(opt));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.findExiledCard(opt.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(opt.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(opt.getId());

        harness.castFromExile(player1, opt.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(opt.getId())).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
