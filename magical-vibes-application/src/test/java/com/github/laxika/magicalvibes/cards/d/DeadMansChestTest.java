package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadMansChestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles cards equal to the dying creature's power and permits nonland casts")
    void exilesFromOwnersLibraryAndAllowsControllerToCastNonland() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Card bear = new GrizzlyBears();
        Card island = new Island();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bear, island, remaining));

        Permanent chest = new Permanent(new DeadMansChest());
        chest.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(chest);

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(bear.getId(), island.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions.get(bear.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(bear.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(island.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bear.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Can enchant only a creature an opponent controls")
    void cannotEnchantOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new DeadMansChest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }
}
