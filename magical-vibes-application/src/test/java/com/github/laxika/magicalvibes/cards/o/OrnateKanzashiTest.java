package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrnateKanzashiTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles the opponent's top card and lets the controller play it this turn")
    void exilesOpponentTopCardAndGrantsPlayPermission() {
        Permanent kanzashi = addReadyKanzashi(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card top = new Island();
        harness.setLibrary(player2, List.of(top, new LlanowarElves()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(kanzashi.isTapped()).isTrue();

        gs.playCardFromExile(gd, player1, top.getId(), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    private Permanent addReadyKanzashi(Player player) {
        Permanent kanzashi = new Permanent(new OrnateKanzashi());
        kanzashi.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(kanzashi);
        return kanzashi;
    }
}
