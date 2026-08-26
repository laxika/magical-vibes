package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ClayFiredBricks;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarketGnome.class, ClayFiredBricks.class, Plains.class, WrathOfGod.class})
class MarketGnomeTest extends BaseCardTest {

    @Test
    @DisplayName("When Market Gnome dies, its controller gains life and draws a card")
    void diesAndGainsLifeAndDraws() {
        harness.addToBattlefield(player1, new MarketGnome());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Plains");
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("When exiled as a craft material, Market Gnome gains life and draws before craft resolves")
    void exiledAsCraftMaterialTriggersBeforeCraftResolves() {
        Permanent bricks = harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        Permanent gnomePermanent = harness.addToBattlefieldAndReturn(player1, new MarketGnome());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(gnomePermanent.getCard().getId())).isNotNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isEqualTo(gnomePermanent.getCard());
        assertThat(gd.stack.getFirst().getCard()).isEqualTo(bricks.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Plains");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bricks);
    }
}
