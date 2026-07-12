package com.github.laxika.magicalvibes.cards.k;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnacksawCliqueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles the opponent's top card and lets the controller play it this turn")
    void exilesOpponentTopCardAndGrantsPlayPermission() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card top = new Island();
        harness.setLibrary(player2, List.of(top, new LlanowarElves()));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent's top card left their library and the controller may play it.
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());

        // The controller can actually play the exiled land onto their own battlefield.
        gs.playCardFromExile(gd, player1, top.getId(), null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Untaps the source when paying {Q}")
    void payingUntapCostUntapsSource() {
        Permanent clique = addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Island()));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(clique.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Island()));

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
